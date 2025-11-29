package com.example.shopgaubong.enums;

public enum Role {
    ADMIN("Quản trị viên"),
    STAFF("Nhân viên"),
    CUSTOMER("Khách hàng");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

