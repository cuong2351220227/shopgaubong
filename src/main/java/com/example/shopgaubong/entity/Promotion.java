package com.example.shopgaubong.entity;

import com.example.shopgaubong.entity.base.AuditEntity;
import com.example.shopgaubong.enums.PromotionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions")
public class Promotion extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Mã khuyến mãi không được để trống")
    @Size(min = 3, max = 50, message = "Mã khuyến mãi phải từ 3-50 ký tự")
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @NotNull(message = "Tên chương trình không được để trống")
    @Size(min = 2, max = 200, message = "Tên chương trình phải từ 2-200 ký tự")
    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Loại khuyến mãi không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromotionType type;

    @NotNull(message = "Giá trị không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị phải > 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal value; // Giá trị giảm (% hoặc số tiền)

    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng tối thiểu phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO; // Giá trị đơn hàng tối thiểu

    @DecimalMin(value = "0.0", message = "Giảm tối đa phải >= 0")
    @Column(precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount; // Giảm tối đa (cho % discount)

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Min(value = 0, message = "Số lần sử dụng tối đa phải >= 0")
    @Column
    private Integer maxUsage; // Số lượt sử dụng tối đa (null = không giới hạn)

    @Column(nullable = false)
    private Integer currentUsage = 0; // Số lượt đã sử dụng

    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    // Constructors
    public Promotion() {
    }

    public Promotion(String code, String name, PromotionType type, BigDecimal value) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    // Business methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        boolean dateValid = now.isAfter(startDate) && now.isBefore(endDate);
        boolean usageValid = maxUsage == null || currentUsage < maxUsage;
        return isActive && dateValid && usageValid;
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isValid() || orderAmount.compareTo(minOrderValue) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (type == PromotionType.PERCENTAGE) {
            discount = orderAmount.multiply(value.divide(BigDecimal.valueOf(100)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                discount = maxDiscountAmount;
            }
        } else {
            discount = value;
        }

        return discount;
    }

    public void incrementUsage() {
        this.currentUsage++;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PromotionType getType() {
        return type;
    }

    public void setType(PromotionType type) {
        this.type = type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getMaxUsage() {
        return maxUsage;
    }

    public void setMaxUsage(Integer maxUsage) {
        this.maxUsage = maxUsage;
    }

    public Integer getCurrentUsage() {
        return currentUsage;
    }

    public void setCurrentUsage(Integer currentUsage) {
        this.currentUsage = currentUsage;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}

