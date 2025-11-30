package com.example.shopgaubong.service;

import com.example.shopgaubong.entity.Account;
import com.example.shopgaubong.entity.Item;
import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.entity.OrderItem;
import com.example.shopgaubong.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Service for managing shopping cart (Orders with CART status)
 */
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private final OrderService orderService;

    public CartService() {
        this.orderService = new OrderService();
    }

    /**
     * Lấy giỏ hàng hiện tại của khách hàng
     */
    public Order getCurrentCart(Long customerId) {
        Order cart = orderService.getActiveCartForCustomer(customerId);
        // Reload with full details to avoid LazyInitializationException
        return orderService.getOrderWithFullDetails(cart.getId()).orElse(cart);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    public void addToCart(Long customerId, Item item, Integer quantity, Long warehouseId) {
        Order cart = getCurrentCart(customerId);
        orderService.addItemToOrder(cart.getId(), item, quantity, warehouseId);
        logger.info("Thêm vào giỏ hàng: {} x{} cho khách hàng ID {}",
                item.getName(), quantity, customerId);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    public void updateCartItem(Long customerId, Long orderItemId, Integer newQuantity, Long warehouseId) {
        Order cart = getCurrentCart(customerId);
        orderService.updateOrderItemQuantity(cart.getId(), orderItemId, newQuantity, warehouseId);
        logger.info("Cập nhật giỏ hàng: OrderItem ID {} -> số lượng {}", orderItemId, newQuantity);
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    public void removeFromCart(Long customerId, Long orderItemId) {
        Order cart = getCurrentCart(customerId);
        orderService.removeItemFromOrder(cart.getId(), orderItemId);
        logger.info("Xóa khỏi giỏ hàng: OrderItem ID {} cho khách hàng ID {}",
                orderItemId, customerId);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void clearCart(Long customerId) {
        Order cart = getCurrentCart(customerId);
        for (OrderItem item : cart.getOrderItems()) {
            orderService.removeItemFromOrder(cart.getId(), item.getId());
        }
        logger.info("Xóa toàn bộ giỏ hàng cho khách hàng ID {}", customerId);
    }

    /**
     * Áp dụng mã khuyến mãi
     */
    public void applyPromotionCode(Long customerId, String promotionCode) {
        Order cart = getCurrentCart(customerId);
        orderService.applyPromotion(cart.getId(), promotionCode);
        logger.info("Áp dụng mã khuyến mãi {} cho khách hàng ID {}", promotionCode, customerId);
    }

    /**
     * Xóa mã khuyến mãi
     */
    public void removePromotionCode(Long customerId) {
        Order cart = getCurrentCart(customerId);
        orderService.removePromotion(cart.getId());
        logger.info("Xóa mã khuyến mãi cho khách hàng ID {}", customerId);
    }

    /**
     * Lấy tổng số lượng sản phẩm trong giỏ
     */
    public Integer getCartItemCount(Long customerId) {
        Order cart = getCurrentCart(customerId);
        return cart.getOrderItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    /**
     * Lấy tổng giá trị giỏ hàng
     */
    public BigDecimal getCartTotal(Long customerId) {
        Order cart = getCurrentCart(customerId);
        return cart.getGrandTotal();
    }

    /**
     * Kiểm tra giỏ hàng có trống không
     */
    public boolean isCartEmpty(Long customerId) {
        Order cart = getCurrentCart(customerId);
        return cart.getOrderItems().isEmpty();
    }

    /**
     * Checkout giỏ hàng
     */
    public Order checkout(Long customerId, String shippingAddress, String shippingCity,
                         String shippingDistrict, String shippingWard,
                         String shippingPhone, String shippingReceiverName, Long warehouseId) {
        Order cart = getCurrentCart(customerId);
        return orderService.checkout(cart.getId(), shippingAddress, shippingCity,
                shippingDistrict, shippingWard, shippingPhone, shippingReceiverName, warehouseId);
    }
}

