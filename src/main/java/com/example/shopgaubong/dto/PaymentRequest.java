package com.example.shopgaubong.dto;

import com.example.shopgaubong.enums.PaymentMethod;

import java.math.BigDecimal;

/**
 * DTO cho yêu cầu tạo thanh toán
 */
public class PaymentRequest {
    private Long orderId;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String returnUrl; // URL quay về sau khi thanh toán (cho gateway)
    private String cancelUrl; // URL khi hủy thanh toán (cho gateway)
    private String ipAddress; // IP của khách hàng
    private String notes;

    // Constructors
    public PaymentRequest() {
    }

    public PaymentRequest(Long orderId, PaymentMethod paymentMethod, BigDecimal amount) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}


