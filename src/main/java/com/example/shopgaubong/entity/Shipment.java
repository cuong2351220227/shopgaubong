package com.example.shopgaubong.entity;

import com.example.shopgaubong.entity.base.AuditEntity;
import com.example.shopgaubong.enums.ShipmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments")
public class Shipment extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Đơn hàng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Mã vận đơn không được để trống")
    @Size(min = 5, max = 50, message = "Mã vận đơn phải từ 5-50 ký tự")
    @Column(unique = true, nullable = false, length = 50)
    private String trackingNumber;

    @NotNull(message = "Nhà vận chuyển không được để trống")
    @Size(max = 100, message = "Tên nhà vận chuyển tối đa 100 ký tự")
    @Column(nullable = false, length = 100)
    private String carrier;

    @NotNull(message = "Trạng thái không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Size(max = 500, message = "Địa chỉ giao hàng tối đa 500 ký tự")
    @Column(length = 500)
    private String shippingAddress;

    @Size(max = 200, message = "Tên người nhận tối đa 200 ký tự")
    @Column(length = 200)
    private String receiverName;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    @Column(length = 20)
    private String receiverPhone;

    @Column(precision = 15, scale = 2)
    private BigDecimal shippingFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight; // kg

    @Column
    private LocalDateTime estimatedDeliveryAt;

    @Column
    private LocalDateTime deliveredAt;

    @Column
    private LocalDateTime shippedAt;

    @Column
    private LocalDateTime pickupDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentTracking> trackingHistory = new ArrayList<>();

    // Constructors
    public Shipment() {
    }

    public Shipment(Order order, String trackingNumber, String carrier) {
        this.order = order;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.status = ShipmentStatus.PENDING;
    }

    // Business methods
    public void updateStatus(ShipmentStatus newStatus, String note) {
        this.status = newStatus;
        
        ShipmentTracking tracking = new ShipmentTracking(this, newStatus, note);
        this.trackingHistory.add(tracking);
        
        if (newStatus == ShipmentStatus.DELIVERED) {
            this.deliveredAt = LocalDateTime.now();
        } else if (newStatus == ShipmentStatus.PICKED_UP) {
            this.pickupDate = LocalDateTime.now();
        } else if (newStatus == ShipmentStatus.IN_TRANSIT && this.shippedAt == null) {
            this.shippedAt = LocalDateTime.now();
        }
    }

    public void markAsShipped() {
        updateStatus(ShipmentStatus.IN_TRANSIT, "Đã bắt đầu vận chuyển");
    }

    public void markAsDelivered() {
        updateStatus(ShipmentStatus.DELIVERED, "Đã giao hàng thành công");
    }

    public boolean isDelivered() {
        return status == ShipmentStatus.DELIVERED;
    }

    public boolean canCancel() {
        return status == ShipmentStatus.PENDING || status == ShipmentStatus.PREPARING;
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

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public LocalDateTime getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDateTime pickupDate) {
        this.pickupDate = pickupDate;
    }

    public List<ShipmentTracking> getTrackingHistory() {
        return trackingHistory;
    }

    public void setTrackingHistory(List<ShipmentTracking> trackingHistory) {
        this.trackingHistory = trackingHistory;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public LocalDateTime getEstimatedDeliveryAt() {
        return estimatedDeliveryAt;
    }

    public void setEstimatedDeliveryAt(LocalDateTime estimatedDeliveryAt) {
        this.estimatedDeliveryAt = estimatedDeliveryAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

