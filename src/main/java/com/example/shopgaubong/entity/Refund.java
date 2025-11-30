package com.example.shopgaubong.entity;

import com.example.shopgaubong.entity.base.AuditEntity;
import com.example.shopgaubong.enums.RefundStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
public class Refund extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Thanh toán không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(unique = true, length = 50)
    private String refundNumber; // Mã hoàn tiền

    @NotNull(message = "Số tiền hoàn không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền hoàn phải > 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @DecimalMin(value = "0.0", message = "Phí hoàn tiền phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal refundFee = BigDecimal.ZERO;

    @NotNull(message = "Trạng thái không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundStatus status = RefundStatus.PENDING;

    @NotNull(message = "Lý do không được để trống")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(length = 200)
    private String gatewayRefundId; // Mã hoàn tiền từ gateway

    @Column(columnDefinition = "TEXT")
    private String gatewayResponse; // Chi tiết phản hồi từ gateway

    @Column
    private LocalDateTime approvedAt;

    @Column(length = 100)
    private String approvedBy;

    @Column
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String adminNotes; // Ghi chú của admin

    @Column(columnDefinition = "TEXT")
    private String rejectReason; // Lý do từ chối

    // Constructors
    public Refund() {
    }

    public Refund(Payment payment, BigDecimal amount, String reason) {
        this.payment = payment;
        this.amount = amount;
        this.reason = reason;
    }

    // Business methods
    public void approve(String approvedBy) {
        this.status = RefundStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(String reason, String rejectedBy) {
        this.status = RefundStatus.REJECTED;
        this.rejectReason = reason;
        this.approvedBy = rejectedBy;
        this.approvedAt = LocalDateTime.now();
    }

    public void markAsProcessing() {
        this.status = RefundStatus.PROCESSING;
    }

    public void markAsCompleted(String gatewayRefundId) {
        this.status = RefundStatus.COMPLETED;
        this.gatewayRefundId = gatewayRefundId;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = RefundStatus.FAILED;
        this.rejectReason = reason;
    }

    public BigDecimal getNetRefundAmount() {
        return amount.subtract(refundFee);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getRefundNumber() {
        return refundNumber;
    }

    public void setRefundNumber(String refundNumber) {
        this.refundNumber = refundNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(BigDecimal refundFee) {
        this.refundFee = refundFee;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getGatewayRefundId() {
        return gatewayRefundId;
    }

    public void setGatewayRefundId(String gatewayRefundId) {
        this.gatewayRefundId = gatewayRefundId;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}

