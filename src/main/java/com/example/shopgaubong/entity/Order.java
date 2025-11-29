package com.example.shopgaubong.entity;

import com.example.shopgaubong.entity.base.AuditEntity;
import com.example.shopgaubong.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String orderNumber; // Mã đơn hàng

    @NotNull(message = "Khách hàng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Account customer;

    @NotNull(message = "Trạng thái không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.CART;

    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(length = 100)
    private String shippingCity;

    @Column(length = 100)
    private String shippingDistrict;

    @Column(length = 100)
    private String shippingWard;

    @Column(length = 15)
    private String shippingPhone;

    @Column(length = 100)
    private String shippingReceiverName;

    @DecimalMin(value = "0.0", message = "Tổng tiền hàng phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO; // Tổng tiền hàng

    @DecimalMin(value = "0.0", message = "Giảm giá phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO; // Tổng giảm giá

    @DecimalMin(value = "0.0", message = "Thuế phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO; // VAT

    @DecimalMin(value = "0.0", message = "Phí ship phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO; // Phí vận chuyển

    @DecimalMin(value = "0.0", message = "Tổng cộng phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO; // Tổng cộng

    @Column(columnDefinition = "TEXT")
    private String notes; // Ghi chú

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Shipment> shipments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion; // Khuyến mãi áp dụng

    // Constructors
    public Order() {
    }

    public Order(Account customer) {
        this.customer = customer;
    }

    // Business methods
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    public void calculateTotals(BigDecimal taxRate) {
        // Tính subtotal
        subtotal = orderItems.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tính discount (item level)
        BigDecimal itemDiscount = orderItems.stream()
                .map(OrderItem::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        discount = itemDiscount;

        // Tính thuế
        BigDecimal taxableAmount = subtotal.subtract(discount);
        tax = taxableAmount.multiply(taxRate).setScale(2, BigDecimal.ROUND_HALF_UP);

        // Tính tổng
        grandTotal = subtotal.subtract(discount).add(tax).add(shippingFee);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Account getCustomer() {
        return customer;
    }

    public void setCustomer(Account customer) {
        this.customer = customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }

    public String getShippingDistrict() {
        return shippingDistrict;
    }

    public void setShippingDistrict(String shippingDistrict) {
        this.shippingDistrict = shippingDistrict;
    }

    public String getShippingWard() {
        return shippingWard;
    }

    public void setShippingWard(String shippingWard) {
        this.shippingWard = shippingWard;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public String getShippingReceiverName() {
        return shippingReceiverName;
    }

    public void setShippingReceiverName(String shippingReceiverName) {
        this.shippingReceiverName = shippingReceiverName;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<Shipment> getShipments() {
        return shipments;
    }

    public void setShipments(List<Shipment> shipments) {
        this.shipments = shipments;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }
}

