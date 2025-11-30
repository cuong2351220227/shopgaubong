package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.OrderDAO;
import com.example.shopgaubong.entity.*;
import com.example.shopgaubong.enums.OrderStatus;
import com.example.shopgaubong.util.HibernateUtil;
import com.example.shopgaubong.util.OrderNumberGenerator;
import com.example.shopgaubong.util.SessionManager;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderDAO orderDAO;
    private final StockService stockService;
    private final PromotionService promotionService;

    // Tax rate (VAT 10%)
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    // Shipping fee calculation
    private static final BigDecimal BASE_SHIPPING_FEE = new BigDecimal("30000"); // 30,000 VND

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.stockService = new StockService();
        this.promotionService = new PromotionService();
    }

    /**
     * Tạo đơn hàng mới (Cart)
     */
    public Order createOrder(Account customer) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CART);
        order.setOrderNumber(OrderNumberGenerator.generate());

        String currentUser = SessionManager.getInstance().getCurrentUsername();
        order.setCreatedBy(currentUser);
        order.setUpdatedBy(currentUser);

        return orderDAO.save(order);
    }

    /**
     * Thêm sản phẩm vào đơn hàng
     */
    public void addItemToOrder(Long orderId, Item item, Integer quantity, Long warehouseId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // [SỬA LỖI] Dùng em.find thay vì orderDAO.findById để giữ kết nối
            Order order = em.find(Order.class, orderId);
            if (order == null) throw new IllegalArgumentException("Đơn hàng không tồn tại");

            // Lúc này order đang nằm trong session 'em', nên gọi hàm này thoải mái
            Hibernate.initialize(order.getOrderItems());

            if (order.getStatus() != OrderStatus.CART) {
                throw new IllegalStateException("Chỉ có thể thêm sản phẩm vào giỏ hàng");
            }

            // Tìm xem sản phẩm đã có trong giỏ chưa
            Optional<OrderItem> existingItem = order.getOrderItems().stream()
                    .filter(oi -> oi.getItem().getId().equals(item.getId()))
                    .findFirst();

            // Tính tổng số lượng cần kiểm tra
            Integer totalQuantityNeeded = quantity;
            if (existingItem.isPresent()) {
                // Nếu đã có trong giỏ, tổng = số lượng hiện tại + số lượng thêm vào
                totalQuantityNeeded = existingItem.get().getQuantity() + quantity;
            }

            // Kiểm tra tồn kho với TỔNG số lượng (không phải chỉ số lượng thêm vào)
            if (!stockService.checkAvailability(warehouseId, item.getId(), totalQuantityNeeded)) {
                Integer availableQty = stockService.getAvailableQuantity(warehouseId, item.getId());
                throw new IllegalStateException(
                    String.format("Không đủ hàng trong kho. Sản phẩm: %s, Tồn kho khả dụng: %d, Tổng yêu cầu: %d (trong giỏ: %d + thêm: %d)",
                    item.getName(), availableQty, totalQuantityNeeded,
                    existingItem.map(OrderItem::getQuantity).orElse(0), quantity));
            }

            if (existingItem.isPresent()) {
                OrderItem orderItem = existingItem.get();
                orderItem.setQuantity(totalQuantityNeeded);
                orderItem.calculateLineTotal();
            } else {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setItem(item);
                orderItem.setQuantity(quantity);
                orderItem.setUnitPrice(item.getPrice());
                orderItem.setDiscount(BigDecimal.ZERO);
                orderItem.calculateLineTotal();

                String currentUser = SessionManager.getInstance().getCurrentUsername();
                orderItem.setCreatedBy(currentUser);
                orderItem.setUpdatedBy(currentUser);

                order.addOrderItem(orderItem);
            }

            order.calculateTotals(TAX_RATE);
            String currentUser = SessionManager.getInstance().getCurrentUsername();
            order.setUpdatedBy(currentUser);

            // Dùng em.merge để cập nhật thay vì orderDAO.update
            em.merge(order);
            em.getTransaction().commit();

            logger.info("Thêm sản phẩm vào đơn hàng {}: {} x{}", order.getOrderNumber(), item.getName(), quantity);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("Lỗi khi thêm sản phẩm: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật số lượng
     */
    public void updateOrderItemQuantity(Long orderId, Long orderItemId, Integer newQuantity, Long warehouseId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // [SỬA LỖI] Dùng em.find
            Order order = em.find(Order.class, orderId);
            if (order == null) throw new IllegalArgumentException("Đơn hàng không tồn tại");

            Hibernate.initialize(order.getOrderItems()); // Load items

            if (order.getStatus() != OrderStatus.CART) {
                throw new IllegalStateException("Chỉ có thể cập nhật giỏ hàng");
            }

            OrderItem orderItem = order.getOrderItems().stream()
                    .filter(oi -> oi.getId().equals(orderItemId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

            // Kiểm tra tồn kho với số lượng mới (đã bao gồm toàn bộ)
            if (!stockService.checkAvailability(warehouseId, orderItem.getItem().getId(), newQuantity)) {
                Integer availableQty = stockService.getAvailableQuantity(warehouseId, orderItem.getItem().getId());
                throw new IllegalStateException(
                    String.format("Không đủ hàng trong kho. Sản phẩm: %s, Tồn kho khả dụng: %d, Số lượng mới yêu cầu: %d (hiện tại trong giỏ: %d)",
                    orderItem.getItem().getName(), availableQty, newQuantity, orderItem.getQuantity()));
            }

            orderItem.setQuantity(newQuantity);
            orderItem.calculateLineTotal();
            order.calculateTotals(TAX_RATE);

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            order.setUpdatedBy(currentUser);
            orderItem.setUpdatedBy(currentUser);

            em.merge(order);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Xóa sản phẩm
     */
    public void removeItemFromOrder(Long orderId, Long orderItemId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // [SỬA LỖI] Dùng em.find
            Order order = em.find(Order.class, orderId);
            if (order == null) throw new IllegalArgumentException("Đơn hàng không tồn tại");

            Hibernate.initialize(order.getOrderItems());

            if (order.getStatus() != OrderStatus.CART) {
                throw new IllegalStateException("Chỉ xóa được trong giỏ hàng");
            }

            OrderItem orderItem = order.getOrderItems().stream()
                    .filter(oi -> oi.getId().equals(orderItemId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

            order.removeOrderItem(orderItem);
            order.calculateTotals(TAX_RATE);

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            order.setUpdatedBy(currentUser);

            em.merge(order);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Áp dụng khuyến mãi
     */
    public void applyPromotion(Long orderId, String promotionCode) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // [SỬA LỖI] Dùng em.find
            Order order = em.find(Order.class, orderId);
            if (order == null) throw new IllegalArgumentException("Đơn hàng không tồn tại");
            Hibernate.initialize(order.getOrderItems());

            if (order.getStatus() != OrderStatus.CART) {
                throw new IllegalStateException("Chỉ áp dụng khuyến mãi cho giỏ hàng");
            }

            Promotion promotion = promotionService.applyPromotion(promotionCode, order.getSubtotal());
            order.setPromotion(promotion);

            BigDecimal promotionDiscount = promotionService.calculateDiscount(promotion, order.getSubtotal());
            order.setDiscount(order.getDiscount().add(promotionDiscount));
            order.calculateTotals(TAX_RATE);

            em.merge(order);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Checkout
     */
    public Order checkout(Long orderId, String shippingAddress, String shippingCity,
                          String shippingDistrict, String shippingWard,
                          String shippingPhone, String shippingReceiverName, Long warehouseId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // [SỬA LỖI] Dùng em.find
            Order order = em.find(Order.class, orderId);
            if (order == null) throw new IllegalArgumentException("Đơn hàng không tồn tại");

            Hibernate.initialize(order.getOrderItems());

            if (order.getStatus() != OrderStatus.CART) throw new IllegalStateException("Đơn hàng đã được đặt");
            if (order.getOrderItems().isEmpty()) throw new IllegalStateException("Giỏ hàng trống");

            order.setShippingAddress(shippingAddress);
            order.setShippingCity(shippingCity);
            order.setShippingDistrict(shippingDistrict);
            order.setShippingWard(shippingWard);
            order.setShippingPhone(shippingPhone);
            order.setShippingReceiverName(shippingReceiverName);

            BigDecimal totalWeight = order.getOrderItems().stream()
                    .map(oi -> {
                        BigDecimal weight = oi.getItem().getWeight() != null ? oi.getItem().getWeight() : BigDecimal.ZERO;
                        return weight.multiply(BigDecimal.valueOf(oi.getQuantity()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setShippingFee(calculateShippingFee(totalWeight));
            order.calculateTotals(TAX_RATE);

            for (OrderItem item : order.getOrderItems()) {
                stockService.reserveStock(warehouseId, item.getItem().getId(), item.getQuantity());
            }

            order.setStatus(OrderStatus.PLACED);
            if (order.getPromotion() != null) {
                promotionService.incrementUsage(order.getPromotion().getId());
            }

            Order updated = em.merge(order);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Các hàm hủy đơn, cập nhật trạng thái giữ nguyên logic nhưng đổi sang dùng em.find() tương tự
    public void cancelOrder(Long orderId, Long warehouseId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Order order = em.find(Order.class, orderId); // [SỬA]
            if (order == null) throw new IllegalArgumentException("Đơn hàng không tồn tại");
            Hibernate.initialize(order.getOrderItems());

            if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CLOSED) {
                throw new IllegalStateException("Không thể hủy đơn hàng đã giao");
            }

            if (order.getStatus() != OrderStatus.CART) {
                for (OrderItem item : order.getOrderItems()) {
                    stockService.releaseStock(warehouseId, item.getItem().getId(), item.getQuantity());
                }
            }
            order.setStatus(OrderStatus.CANCELED);
            em.merge(order);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateOrderStatus(Long orderId, OrderStatus newStatus, Long warehouseId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Order order = em.find(Order.class, orderId); // [SỬA]
            if (order == null) throw new IllegalArgumentException("Đơn hàng không tồn tại");

            if (newStatus == OrderStatus.SHIPPED) Hibernate.initialize(order.getOrderItems());

            validateStatusTransition(order.getStatus(), newStatus);

            if (newStatus == OrderStatus.SHIPPED) {
                for (OrderItem item : order.getOrderItems()) {
                    stockService.commitStock(warehouseId, item.getItem().getId(), item.getQuantity());
                }
            }
            order.setStatus(newStatus);
            em.merge(order);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // --- Các hàm tiện ích giữ nguyên ---
    private BigDecimal calculateShippingFee(BigDecimal totalWeight) {
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) return BASE_SHIPPING_FEE;
        BigDecimal additionalFee = totalWeight.subtract(BigDecimal.ONE)
                .max(BigDecimal.ZERO)
                .multiply(new BigDecimal("5000"))
                .setScale(0, RoundingMode.UP);
        return BASE_SHIPPING_FEE.add(additionalFee);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = false;
        switch (current) {
            case CART: valid = next == OrderStatus.PLACED; break;
            case PLACED: valid = next == OrderStatus.PENDING_PAYMENT || next == OrderStatus.CANCELED; break;
            case PENDING_PAYMENT: valid = next == OrderStatus.PAID || next == OrderStatus.CANCELED; break;
            case PAID: valid = next == OrderStatus.PACKED || next == OrderStatus.CANCELED; break;
            case PACKED: valid = next == OrderStatus.SHIPPED; break;
            case SHIPPED: valid = next == OrderStatus.DELIVERED; break;
            case DELIVERED: valid = next == OrderStatus.CLOSED || next == OrderStatus.RMA_REQUESTED; break;
            case RMA_REQUESTED: valid = next == OrderStatus.REFUNDED; break;
        }
        if (!valid) throw new IllegalStateException(String.format("Không thể chuyển từ trạng thái %s sang %s", current, next));
    }

    // --- Các hàm Query với Eager Loading để tránh LazyInitializationException ---

    /**
     * Tìm Order theo ID với đầy đủ thông tin liên quan
     */
    public Optional<Order> getOrderById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "LEFT JOIN FETCH o.promotion " +
                    "WHERE o.id = :id";

            var query = em.createQuery(jpql, Order.class);
            query.setParameter("id", id);
            List<Order> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Lỗi khi tìm Order theo ID {}: {}", id, e.getMessage());
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm Order theo orderNumber
     */
    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderDAO.findByOrderNumber(orderNumber);
    }

    /**
     * Tìm các Order theo customer
     */
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderDAO.findByCustomerId(customerId);
    }

    /**
     * Tìm các Order theo trạng thái
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderDAO.findByStatus(status);
    }

    /**
     * Lấy tất cả Orders với đầy đủ thông tin liên quan
     */
    public List<Order> getAllOrders() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "LEFT JOIN FETCH o.promotion " +
                    "ORDER BY o.createdAt DESC";

            var query = em.createQuery(jpql, Order.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy tất cả Orders: {}", e.getMessage());
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy giỏ hàng hiện tại của customer hoặc tạo mới
     */
    public Order getActiveCartForCustomer(Long customerId) {
        List<Order> carts = orderDAO.findByCustomerIdAndStatus(customerId, OrderStatus.CART);
        if (carts.isEmpty()) {
            // Tạo Account entity với đầy đủ thông tin để tránh LazyInitializationException
            EntityManager em = HibernateUtil.getEntityManager();
            try {
                Account customer = em.find(Account.class, customerId);
                if (customer == null) {
                    throw new IllegalArgumentException("Khách hàng không t��n tại với ID: " + customerId);
                }
                // Initialize profile nếu cần
                Hibernate.initialize(customer.getProfile());
                return createOrder(customer);
            } finally {
                em.close();
            }
        }
        return carts.get(0);
    }

    /**
     * Tìm Orders theo customer với eager loading
     */
    public List<Order> findOrdersByCustomer(Long customerId) {
        return orderDAO.findByCustomerId(customerId);
    }

    /**
     * Tìm Order với đầy đủ thông tin để hiển thị chi tiết
     */
    public Optional<Order> getOrderWithFullDetails(Long orderId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "LEFT JOIN FETCH o.promotion p " +
                    "LEFT JOIN FETCH o.payments pay " +
                    "WHERE o.id = :orderId";

            var query = em.createQuery(jpql, Order.class);
            query.setParameter("orderId", orderId);
            List<Order> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Lỗi khi lấy Order với full details {}: {}", orderId, e.getMessage());
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm Orders theo trạng thái với phân trang
     */
    public List<Order> getOrdersByStatusWithPaging(OrderStatus status, int page, int size) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "WHERE o.status = :status " +
                    "ORDER BY o.createdAt DESC";

            var query = em.createQuery(jpql, Order.class);
            query.setParameter("status", status);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy Orders theo status với paging: {}", e.getMessage());
            return List.of();
        } finally {
            em.close();
        }
    }
}