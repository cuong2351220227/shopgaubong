package com.example.shopgaubong.dto;

import com.example.shopgaubong.entity.Item;
import com.example.shopgaubong.entity.StockItem;
import com.example.shopgaubong.entity.Warehouse;

import java.math.BigDecimal;

/**
 * DTO để hiển thị thông tin tồn kho
 */
public class StockItemDTO {

    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private Long itemId;
    private String itemName;
    private String categoryName;
    private BigDecimal itemPrice;
    private Integer onHand;
    private Integer reserved;
    private Integer available;
    private Integer lowStockThreshold;
    private Boolean isLowStock;

    public StockItemDTO() {
    }

    public StockItemDTO(StockItem stockItem) {
        this.id = stockItem.getId();

        Warehouse warehouse = stockItem.getWarehouse();
        if (warehouse != null) {
            this.warehouseId = warehouse.getId();
            this.warehouseName = warehouse.getName();
        }

        Item item = stockItem.getItem();
        if (item != null) {
            this.itemId = item.getId();
            this.itemName = item.getName();
            this.itemPrice = item.getPrice();
            if (item.getCategory() != null) {
                this.categoryName = item.getCategory().getName();
            }
        }

        this.onHand = stockItem.getOnHand();
        this.reserved = stockItem.getReserved();
        this.available = stockItem.getAvailable();
        this.lowStockThreshold = stockItem.getLowStockThreshold();
        this.isLowStock = stockItem.isLowStock();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
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

    public Integer getAvailable() {
        return available;
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public Boolean getIsLowStock() {
        return isLowStock;
    }

    public void setIsLowStock(Boolean isLowStock) {
        this.isLowStock = isLowStock;
    }
}

