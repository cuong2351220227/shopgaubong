package com.example.shopgaubong.entity;

import com.example.shopgaubong.entity.base.AuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "stock_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"warehouse_id", "item_id"})
})
public class StockItem extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Kho không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @NotNull(message = "Sản phẩm không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho phải >= 0")
    @Column(nullable = false)
    private Integer onHand = 0; // Số lượng thực tế trong kho

    @NotNull(message = "Số lượng giữ chỗ không được để trống")
    @Min(value = 0, message = "Số lượng giữ chỗ phải >= 0")
    @Column(nullable = false)
    private Integer reserved = 0; // Số lượng đã được giữ chỗ (đơn hàng đang xử lý)

    @Min(value = 0, message = "Ngưỡng cảnh báo phải >= 0")
    @Column(nullable = false)
    private Integer lowStockThreshold = 10; // Ngưỡng cảnh báo tồn kho thấp

    // Constructors
    public StockItem() {
    }

    public StockItem(Warehouse warehouse, Item item, Integer onHand) {
        this.warehouse = warehouse;
        this.item = item;
        this.onHand = onHand;
    }

    // Business methods
    public Integer getAvailable() {
        return onHand - reserved;
    }

    public boolean isLowStock() {
        return getAvailable() <= lowStockThreshold;
    }

    public void reserveStock(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (getAvailable() < quantity) {
            throw new IllegalStateException("Không đủ hàng tồn kho");
        }
        this.reserved += quantity;
    }

    public void releaseStock(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (this.reserved < quantity) {
            throw new IllegalStateException("Số lượng giải phóng vượt quá số lượng đã giữ");
        }
        this.reserved -= quantity;
    }

    public void commitStock(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (this.reserved < quantity || this.onHand < quantity) {
            throw new IllegalStateException("Không thể xác nhận số lượng");
        }
        this.reserved -= quantity;
        this.onHand -= quantity;
    }

    public void addStock(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        this.onHand += quantity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Integer getOnHand() {
        return onHand;
    }

    public void setOnHand(Integer onHand) {
        this.onHand = onHand;
    }

    public Integer getReserved() {
        return reserved;
    }

    public void setReserved(Integer reserved) {
        this.reserved = reserved;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
}

