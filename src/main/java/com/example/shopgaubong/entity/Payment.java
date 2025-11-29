package com.example.shopgaubong.entity;

import com.example.shopgaubong.entity.base.AuditEntity;
import com.example.shopgaubong.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Đơn hàng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền phải > 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @DecimalMin(value = "0.0", message = "Phí COD phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal codFee = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Phí gateway phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal gatewayFee = BigDecimal.ZERO;

    @Column(length = 100)
    private String transactionId; // Mã giao dịch

    @Column(nullable = false)
    private Boolean isPaid = false;

    @Column
    private LocalDateTime paidAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Constructors
    public Payment() {
    }

    public Payment(Order order, PaymentMethod method, BigDecimal amount) {
        this.order = order;
        this.method = method;
        this.amount = amount;
    }

    // Business methods
    public void markAsPaid() {
        this.isPaid = true;
        this.paidAt = LocalDateTime.now();
    }

    public BigDecimal getTotalAmount() {
        return amount.add(codFee).add(gatewayFee);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

