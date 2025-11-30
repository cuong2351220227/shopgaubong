package com.example.shopgaubong.enums;

public enum RefundStatus {
    PENDING("Chờ xử lý"),
    APPROVED("Đã duyệt"),
    PROCESSING("Đang xử lý"),
    COMPLETED("Đã hoàn tiền"),
    REJECTED("Từ chối"),
    FAILED("Thất bại");

    private final String displayName;

    RefundStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

