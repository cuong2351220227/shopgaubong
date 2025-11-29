package com.example.shopgaubong.enums;

public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng"),
    BANK_TRANSFER("Chuyển khoản ngân hàng"),
    GATEWAY("Cổng thanh toán"),
    WALLET("Ví nội bộ");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

