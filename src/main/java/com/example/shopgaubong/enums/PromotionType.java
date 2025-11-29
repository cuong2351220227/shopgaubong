package com.example.shopgaubong.enums;

public enum PromotionType {
    PERCENTAGE("Giảm theo phần trăm"),
    FIXED_AMOUNT("Giảm số tiền cố định");

    private final String displayName;

    PromotionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

