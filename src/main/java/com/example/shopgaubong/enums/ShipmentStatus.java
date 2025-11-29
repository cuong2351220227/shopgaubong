package com.example.shopgaubong.enums;

public enum ShipmentStatus {
    PENDING("Chờ xử lý"),
    PICKED_UP("Đã lấy hàng"),
    IN_TRANSIT("Đang vận chuyển"),
    OUT_FOR_DELIVERY("Đang giao hàng"),
    DELIVERED("Đã giao"),
    FAILED("Giao thất bại"),
    RETURNED("Đã trả lại");

    private final String displayName;

    ShipmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

