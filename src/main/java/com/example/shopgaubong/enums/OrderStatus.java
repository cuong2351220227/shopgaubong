package com.example.shopgaubong.enums;

public enum OrderStatus {
    CART("Giỏ hàng"),
    PLACED("Đã đặt"),
    PENDING_PAYMENT("Chờ thanh toán"),
    PAID("Đã thanh toán"),
    PACKED("Đã đóng gói"),
    SHIPPED("Đang giao"),
    DELIVERED("Đã giao"),
    CLOSED("Hoàn tất"),
    CANCELED("Đã hủy"),
    RMA_REQUESTED("Yêu cầu hoàn trả"),
    REFUNDED("Đã hoàn tiền");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

