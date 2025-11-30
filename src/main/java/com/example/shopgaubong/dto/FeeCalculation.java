package com.example.shopgaubong.dto;

import java.math.BigDecimal;

/**
 * DTO cho tính toán chi tiết phí
 */
public class FeeCalculation {
    private BigDecimal subtotal; // Tiền hàng
    private BigDecimal codFee; // Phí COD
    private BigDecimal gatewayFee; // Phí gateway
    private BigDecimal transactionFee; // Phí giao dịch
    private BigDecimal processingFee; // Phí xử lý
    private BigDecimal shippingFee; // Phí vận chuyển
    private BigDecimal tax; // Thuế VAT
    private BigDecimal discount; // Giảm giá
    private BigDecimal totalFees; // Tổng phí
    private BigDecimal grandTotal; // Tổng cộng

    // Constructor
    public FeeCalculation() {
        this.codFee = BigDecimal.ZERO;
        this.gatewayFee = BigDecimal.ZERO;
        this.transactionFee = BigDecimal.ZERO;
        this.processingFee = BigDecimal.ZERO;
        this.shippingFee = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
    }

    public void calculateTotals() {
        this.totalFees = codFee.add(gatewayFee).add(transactionFee).add(processingFee);
        this.grandTotal = subtotal
                .add(totalFees)
                .add(shippingFee)
                .add(tax)
                .subtract(discount);
    }

    // Getters and Setters
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getCodFee() {
        return codFee;
    }

    public void setCodFee(BigDecimal codFee) {
        this.codFee = codFee;
    }

    public BigDecimal getGatewayFee() {
        return gatewayFee;
    }

    public void setGatewayFee(BigDecimal gatewayFee) {
        this.gatewayFee = gatewayFee;
    }

    public BigDecimal getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(BigDecimal transactionFee) {
        this.transactionFee = transactionFee;
    }

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(BigDecimal totalFees) {
        this.totalFees = totalFees;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }
}

