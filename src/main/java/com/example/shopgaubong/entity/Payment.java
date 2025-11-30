package com.example.shopgaubong.entity;

import com.example.shopgaubong.entity.base.AuditEntity;
import com.example.shopgaubong.enums.PaymentMethod;
import com.example.shopgaubong.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
public class Payment extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Đơn hàng không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @NotNull(message = "Trạng thái không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

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

    @DecimalMin(value = "0.0", message = "Phí giao dịch phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal transactionFee = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Phí xử lý phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal processingFee = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Số tiền hoàn phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(length = 100)
    private String transactionId; // Mã giao dịch

    @Column(length = 200)
    private String gatewayTransactionId; // Mã giao dịch từ gateway (VNPay, MoMo, SePay)

    @Column(length = 200)
    private String gatewayResponseCode; // Mã phản hồi từ gateway

    @Column(columnDefinition = "TEXT")
    private String gatewayResponse; // Chi tiết phản hồi từ gateway

    @Column(nullable = false)
    private Boolean isPaid = false;

    @Column
    private LocalDateTime paidAt;

    @Column
    private LocalDateTime expiredAt; // Thời hạn thanh toán

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Refund> refunds = new ArrayList<>();

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
        this.status = PaymentStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.notes = (this.notes != null ? this.notes + "\n" : "") + "Lỗi: " + reason;
    }

    public void markAsCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }

    public BigDecimal getTotalFees() {
        return codFee.add(gatewayFee).add(transactionFee).add(processingFee);
    }

    public BigDecimal getTotalAmount() {
        return amount.add(getTotalFees());
    }

    public BigDecimal getRefundableAmount() {
        return amount.subtract(refundedAmount);
    }

    public boolean isRefundable() {
        return isPaid && status == PaymentStatus.COMPLETED
                && refundedAmount.compareTo(amount) < 0;
    }

    public void addRefund(Refund refund) {
        refunds.add(refund);
        refund.setPayment(this);
        this.refundedAmount = this.refundedAmount.add(refund.getAmount());

        if (this.refundedAmount.compareTo(this.amount) >= 0) {
            this.status = PaymentStatus.REFUNDED;
        } else if (this.refundedAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = PaymentStatus.PARTIAL_REFUNDED;
        }
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
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

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public String getGatewayResponseCode() {
        return gatewayResponseCode;
    }

    public void setGatewayResponseCode(String gatewayResponseCode) {
        this.gatewayResponseCode = gatewayResponseCode;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
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

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Refund> getRefunds() {
        return refunds;
    }

    public void setRefunds(List<Refund> refunds) {
        this.refunds = refunds;
    }
}

