package com.example.shopgaubong.dto;

import java.math.BigDecimal;

/**
 * DTO cho yêu cầu hoàn tiền
 */
public class RefundRequest {
    private Long paymentId;
    private BigDecimal amount;
    private String reason;
    private String notes;

    // Constructors
    public RefundRequest() {
    }

    public RefundRequest(Long paymentId, BigDecimal amount, String reason) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.reason = reason;
    }

    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

