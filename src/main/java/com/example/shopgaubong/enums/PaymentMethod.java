package com.example.shopgaubong.enums;

public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng", 0.02, 0.0), // 2% phí COD
    BANK_TRANSFER("Chuyển khoản ngân hàng", 0.0, 0.0), // Không phí
    VNPAY("VNPay Gateway", 0.0, 0.022), // 2.2% phí gateway
    MOMO("MoMo Wallet", 0.0, 0.025), // 2.5% phí gateway
    SEPAY("SePay Gateway", 0.0, 0.018), // 1.8% phí gateway
    WALLET("Ví nội bộ", 0.0, 0.0); // Không phí

    private final String displayName;
    private final double codFeeRate; // Tỷ lệ phí COD
    private final double gatewayFeeRate; // Tỷ lệ phí gateway

    PaymentMethod(String displayName, double codFeeRate, double gatewayFeeRate) {
        this.displayName = displayName;
        this.codFeeRate = codFeeRate;
        this.gatewayFeeRate = gatewayFeeRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getCodFeeRate() {
        return codFeeRate;
    }

    public double getGatewayFeeRate() {
        return gatewayFeeRate;
    }

    public boolean isGateway() {
        return this == VNPAY || this == MOMO || this == SEPAY;
    }

    public boolean isWallet() {
        return this == MOMO || this == WALLET;
    }
}

