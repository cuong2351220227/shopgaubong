package com.example.shopgaubong.entity;

import com.example.shopgaubong.entity.base.AuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Đơn hàng không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Sản phẩm không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải >= 1")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn giá phải >= 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Giảm giá phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    public OrderItem() {
    }

    public OrderItem(Item item, Integer quantity, BigDecimal unitPrice) {
        this.item = item;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateLineTotal();
    }

    // --- [FIX QUAN TRỌNG] Sửa phương thức này để tránh NullPointerException ---
    public void calculateLineTotal() {
        // Kiểm tra nếu giá hoặc số lượng chưa có thì gán bằng 0 để tránh lỗi
        if (this.unitPrice == null || this.quantity == null) {
            this.lineTotal = BigDecimal.ZERO;
            return;
        }

        BigDecimal safeDiscount = (this.discount == null) ? BigDecimal.ZERO : this.discount;
        BigDecimal total = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        this.lineTotal = total.subtract(safeDiscount);

        // Đảm bảo không âm
        if (this.lineTotal.compareTo(BigDecimal.ZERO) < 0) {
            this.lineTotal = BigDecimal.ZERO;
        }
    }
    // --------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public Integer getQuantity() { return quantity; }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateLineTotal(); // Tính lại ngay khi set
    }

    public BigDecimal getUnitPrice() { return unitPrice; }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateLineTotal(); // Tính lại ngay khi set
    }

    public BigDecimal getDiscount() { return discount; }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
        calculateLineTotal(); // Tính lại ngay khi set
    }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}