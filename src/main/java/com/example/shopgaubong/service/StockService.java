package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.StockItemDAO;
import com.example.shopgaubong.entity.Warehouse;
import com.example.shopgaubong.entity.Item;
import com.example.shopgaubong.entity.StockItem;
import com.example.shopgaubong.util.HibernateUtil;
import com.example.shopgaubong.util.SessionManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    private final StockItemDAO stockItemDAO;

    public StockService() {
        this.stockItemDAO = new StockItemDAO();
    }

    /**
     * Tạo stock item mới
     */
    public StockItem createStockItem(Long warehouseId, Long itemId, Integer onHand, Integer lowStockThreshold) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Check if stock item already exists
            TypedQuery<StockItem> q = em.createQuery(
                    "SELECT s FROM StockItem s WHERE s.warehouse.id = :warehouseId AND s.item.id = :itemId", StockItem.class);
            q.setParameter("warehouseId", warehouseId);
            q.setParameter("itemId", itemId);
            List<StockItem> existing = q.getResultList();
            if (!existing.isEmpty()) {
                throw new IllegalArgumentException("Sản phẩm đã tồn tại trong kho này");
            }

            Warehouse warehouse = em.find(Warehouse.class, warehouseId);
            if (warehouse == null) throw new IllegalArgumentException("Kho không tồn tại");

            Item item = em.find(Item.class, itemId);
            if (item == null) throw new IllegalArgumentException("Sản phẩm không tồn tại");

            StockItem stockItem = new StockItem();
            stockItem.setWarehouse(warehouse);
            stockItem.setItem(item);
            stockItem.setOnHand(onHand);
            stockItem.setReserved(0);
            stockItem.setLowStockThreshold(lowStockThreshold != null ? lowStockThreshold : 10);

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            stockItem.setCreatedBy(currentUser);
            stockItem.setUpdatedBy(currentUser);

            em.persist(stockItem);
            em.getTransaction().commit();

            logger.info("Tạo tồn kho mới: {} - {} (số lượng: {})", warehouse.getName(), item.getName(), onHand);
            return stockItem;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Lỗi khi tạo tồn kho: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật số lượng tồn kho
     */
    public StockItem updateStockItem(Long id, Integer onHand, Integer lowStockThreshold) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            StockItem stockItem = stockItemDAO.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tồn kho không tồn tại"));

            stockItem.setOnHand(onHand);
            stockItem.setLowStockThreshold(lowStockThreshold);

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            stockItem.setUpdatedBy(currentUser);

            StockItem updated = stockItemDAO.update(stockItem);
            em.getTransaction().commit();

            logger.info("Cập nhật tồn kho: {} (số lượng: {})", stockItem.getItem().getName(), onHand);
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Lỗi khi cập nhật tồn kho: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Thêm hàng vào kho
     */
    public StockItem addStock(Long id, Integer quantity) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            StockItem stockItem = stockItemDAO.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tồn kho không tồn tại"));

            stockItem.addStock(quantity);

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            stockItem.setUpdatedBy(currentUser);

            StockItem updated = stockItemDAO.update(stockItem);
            em.getTransaction().commit();

            logger.info("Nhập kho: {} - {} (số lượng: {})",
                    stockItem.getWarehouse().getName(), stockItem.getItem().getName(), quantity);
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Lỗi khi nhập kho: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Giữ chỗ hàng trong kho (khi tạo đơn hàng)
     * Nếu kho yêu cầu không đủ, sẽ cố gắng giữ từ các kho khác (chia nhỏ nếu cần).
     */
    public void reserveStock(Long warehouseId, Long itemId, Integer quantity) {
        if (quantity == null || quantity <= 0) return;
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // 1) Try to lock requested warehouse stock first
            TypedQuery<StockItem> qRequested = em.createQuery(
                    "SELECT s FROM StockItem s WHERE s.warehouse.id = :warehouseId AND s.item.id = :itemId", StockItem.class);
            qRequested.setParameter("warehouseId", warehouseId);
            qRequested.setParameter("itemId", itemId);
            qRequested.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            List<StockItem> requestedResults = qRequested.getResultList();

            int remaining = quantity;

            if (!requestedResults.isEmpty()) {
                StockItem primary = requestedResults.get(0);
                int onHand = primary.getOnHand() != null ? primary.getOnHand() : 0;
                int reserved = primary.getReserved() != null ? primary.getReserved() : 0;
                int available = onHand - reserved;

                int take = Math.min(available, remaining);
                if (take > 0) {
                    primary.setReserved(reserved + take);
                    primary.setUpdatedBy(SessionManager.getInstance().getCurrentUsername());
                    em.merge(primary);
                    remaining -= take;
                }
            } else {
                // If record doesn't exist in requested warehouse, we may create later if needed.
            }

            // If still need more, lock other warehouses and try to fulfill
            if (remaining > 0) {
                TypedQuery<StockItem> qAll = em.createQuery(
                        "SELECT s FROM StockItem s WHERE s.item.id = :itemId ORDER BY (s.onHand - s.reserved) DESC", StockItem.class);
                qAll.setParameter("itemId", itemId);
                qAll.setLockMode(LockModeType.PESSIMISTIC_WRITE);
                List<StockItem> allStocks = qAll.getResultList();

                for (StockItem s : allStocks) {
                    // Skip the primary warehouse if already handled
                    if (s.getWarehouse().getId().equals(warehouseId)) continue;
                    int onHand = s.getOnHand() != null ? s.getOnHand() : 0;
                    int reserved = s.getReserved() != null ? s.getReserved() : 0;
                    int available = onHand - reserved;
                    if (available <= 0) continue;
                    int take = Math.min(available, remaining);
                    s.setReserved(reserved + take);
                    s.setUpdatedBy(SessionManager.getInstance().getCurrentUsername());
                    em.merge(s);
                    remaining -= take;
                    if (remaining <= 0) break;
                }
            }

            // If still remaining > 0, no sufficient stock across warehouses
            if (remaining > 0) {
                // Optional: rollback any partial reservations we made earlier in this tx
                em.getTransaction().rollback();
                throw new IllegalStateException(String.format("Không đủ hàng trong kho. Sản phẩm ID: %d, Yêu cầu: %d, Không đủ: %d",
                        itemId, quantity, remaining));
            }

            // If we reserved from no existing stock (e.g. no StockItem existed anywhere) but remaining == 0 is false already handled
            // Persist any newly created stock items if needed (not creating new here because we reserve from existing stocks)

            em.getTransaction().commit();

            logger.info("Giữ chỗ tồn kho cho Item ID {}: tổng giữ = {}", itemId, quantity);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("Lỗi khi giữ chỗ tồn kho: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Giải phóng hàng đã giữ chỗ (khi hủy đơn hàng)
     */
    public void releaseStock(Long warehouseId, Long itemId, Integer quantity) {
        if (quantity == null || quantity <= 0) return;
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            TypedQuery<StockItem> q = em.createQuery(
                    "SELECT s FROM StockItem s WHERE s.warehouse.id = :warehouseId AND s.item.id = :itemId", StockItem.class);
            q.setParameter("warehouseId", warehouseId);
            q.setParameter("itemId", itemId);
            q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            List<StockItem> results = q.getResultList();
            if (results.isEmpty()) {
                logger.warn("Không tìm thấy StockItem cho Warehouse ID {} và Item ID {}. Bỏ qua việc giải phóng.",
                        warehouseId, itemId);
                em.getTransaction().commit();
                return;
            }

            StockItem stockItem = results.get(0);
            int reserved = stockItem.getReserved() != null ? stockItem.getReserved() : 0;
            if (reserved < quantity) {
                logger.warn("Yêu cầu giải phóng vượt quá reserved. Reserved: {}, Yêu cầu: {}", reserved, quantity);
                quantity = reserved; // release whatever is reserved
            }
            stockItem.setReserved(Math.max(0, reserved - quantity));
            stockItem.setUpdatedBy(SessionManager.getInstance().getCurrentUsername());
            em.merge(stockItem);
            em.getTransaction().commit();

            logger.info("Giải phóng tồn kho: {} - {} (số lượng: {})",
                    stockItem.getWarehouse().getName(), stockItem.getItem().getName(), quantity);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("Lỗi khi giải phóng tồn kho: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Xác nhận xuất kho (khi giao hàng)
     */
    public void commitStock(Long warehouseId, Long itemId, Integer quantity) {
        if (quantity == null || quantity <= 0) return;
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            TypedQuery<StockItem> q = em.createQuery(
                    "SELECT s FROM StockItem s WHERE s.warehouse.id = :warehouseId AND s.item.id = :itemId", StockItem.class);
            q.setParameter("warehouseId", warehouseId);
            q.setParameter("itemId", itemId);
            q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            List<StockItem> results = q.getResultList();
            if (results.isEmpty()) throw new IllegalStateException("Sản phẩm không tồn tại trong kho.");

            StockItem stockItem = results.get(0);
            int onHand = stockItem.getOnHand() != null ? stockItem.getOnHand() : 0;
            int reserved = stockItem.getReserved() != null ? stockItem.getReserved() : 0;

            if (reserved < quantity || onHand < quantity) {
                throw new IllegalStateException("Không thể commit, số lượng không hợp lệ.");
            }

            stockItem.setOnHand(onHand - quantity);
            stockItem.setReserved(reserved - quantity);
            stockItem.setUpdatedBy(SessionManager.getInstance().getCurrentUsername());
            em.merge(stockItem);

            em.getTransaction().commit();

            logger.info("Xuất kho: {} - {} (số lượng: {})",
                    stockItem.getWarehouse().getName(), stockItem.getItem().getName(), quantity);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("Lỗi khi xuất kho: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả tồn kho
     */
    public List<StockItem> getAllStockItems() {
        return stockItemDAO.findAll();
    }

    /**
     * Lấy tồn kho theo kho
     */
    public List<StockItem> getStockItemsByWarehouse(Long warehouseId) {
        return stockItemDAO.findByWarehouse(warehouseId);
    }

    /**
     * Lấy tồn kho theo sản phẩm
     */
    public List<StockItem> getStockItemsByItem(Long itemId) {
        return stockItemDAO.findByItem(itemId);
    }

    /**
     * Lấy danh sách sản phẩm tồn kho thấp
     */
    public List<StockItem> getLowStockItems() {
        return stockItemDAO.findLowStock();
    }

    /**
     * Kiểm tra tồn kho có sẵn (sử dụng same EM & PESSIMISTIC_READ)
     */
    public boolean checkAvailability(Long warehouseId, Long itemId, Integer quantity) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<StockItem> q = em.createQuery(
                    "SELECT s FROM StockItem s WHERE s.warehouse.id = :warehouseId AND s.item.id = :itemId", StockItem.class);
            q.setParameter("warehouseId", warehouseId);
            q.setParameter("itemId", itemId);
            q.setLockMode(LockModeType.PESSIMISTIC_READ);
            List<StockItem> results = q.getResultList();
            if (results.isEmpty()) {
                em.getTransaction().commit();
                logger.warn("Không tìm thấy StockItem cho Warehouse ID {} và Item ID {}", warehouseId, itemId);
                return false;
            }
            StockItem s = results.get(0);
            int onHand = s.getOnHand() != null ? s.getOnHand() : 0;
            int reserved = s.getReserved() != null ? s.getReserved() : 0;
            int available = onHand - reserved;
            em.getTransaction().commit();
            boolean result = available >= (quantity != null ? quantity : 0);
            logger.debug("Check availability: Item ID {}, On Hand: {}, Reserved: {}, Available: {}, Required: {}, Result: {}",
                    itemId, onHand, reserved, available, quantity, result);
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("checkAvailability error: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy số lượng tồn kho khả dụng
     */
    public Integer getAvailableQuantity(Long warehouseId, Long itemId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<StockItem> q = em.createQuery(
                    "SELECT s FROM StockItem s WHERE s.warehouse.id = :warehouseId AND s.item.id = :itemId", StockItem.class);
            q.setParameter("warehouseId", warehouseId);
            q.setParameter("itemId", itemId);
            q.setLockMode(LockModeType.PESSIMISTIC_READ);
            List<StockItem> results = q.getResultList();
            if (results.isEmpty()) {
                em.getTransaction().commit();
                logger.warn("Không tìm thấy StockItem cho Warehouse ID {} và Item ID {} khi lấy available quantity",
                        warehouseId, itemId);
                return 0;
            }
            StockItem s = results.get(0);
            int onHand = s.getOnHand() != null ? s.getOnHand() : 0;
            int reserved = s.getReserved() != null ? s.getReserved() : 0;
            int available = Math.max(0, onHand - reserved);
            em.getTransaction().commit();
            logger.debug("Get available quantity: Item ID {}, On Hand: {}, Reserved: {}, Available: {}",
                    itemId, onHand, reserved, available);
            return available;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("getAvailableQuantity error: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy StockItem, tạo mới nếu chưa tồn tại
     */
    public StockItem getOrCreateStockItem(Long warehouseId, Long itemId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<StockItem> q = em.createQuery(
                    "SELECT s FROM StockItem s WHERE s.warehouse.id = :warehouseId AND s.item.id = :itemId", StockItem.class);
            q.setParameter("warehouseId", warehouseId);
            q.setParameter("itemId", itemId);
            q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            List<StockItem> results = q.getResultList();
            if (!results.isEmpty()) {
                StockItem found = results.get(0);
                em.getTransaction().commit();
                return found;
            }

            // create new
            Warehouse warehouse = em.find(Warehouse.class, warehouseId);
            if (warehouse == null) throw new IllegalArgumentException("Kho không tồn tại");
            Item item = em.find(Item.class, itemId);
            if (item == null) throw new IllegalArgumentException("Sản phẩm không tồn tại");

            StockItem stockItem = new StockItem();
            stockItem.setWarehouse(warehouse);
            stockItem.setItem(item);
            stockItem.setOnHand(0);
            stockItem.setReserved(0);
            stockItem.setLowStockThreshold(10);

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            stockItem.setCreatedBy(currentUser);
            stockItem.setUpdatedBy(currentUser);

            em.persist(stockItem);
            em.getTransaction().commit();
            return stockItem;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra xem sản phẩm đã có trong kho chưa
     */
    public boolean isItemInWarehouse(Long warehouseId, Long itemId) {
        return stockItemDAO.findByWarehouseAndItem(warehouseId, itemId).isPresent();
    }

    public void deleteStockItem(Long id) {
        // TODO: Implement delete logic if needed
    }
}
