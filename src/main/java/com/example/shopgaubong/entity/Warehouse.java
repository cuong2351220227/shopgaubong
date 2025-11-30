package com.example.shopgaubong.entity;

import com.example.shopgaubong.entity.base.AuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "warehouses")
public class Warehouse extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Mã kho không được để trống")
    @Size(min = 2, max = 50, message = "Mã kho phải từ 2-50 ký tự")
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @NotNull(message = "Tên kho không được để trống")
    @Size(min = 2, max = 100, message = "Tên kho phải từ 2-100 ký tự")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Size(max = 100, message = "Thành phố không quá 100 ký tự")
    @Column(length = 100)
    private String city;

    @Size(max = 100, message = "Quận/huyện không quá 100 ký tự")
    @Column(length = 100)
    private String district;

    @Size(max = 100, message = "Phường/xã không quá 100 ký tự")
    @Column(length = 100)
    private String ward;

    @Size(max = 20, message = "Số điện thoại không quá 20 ký tự")
    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<StockItem> stockItems = new ArrayList<>();

    // Constructors
    public Warehouse() {
    }

    public Warehouse(String code, String name) {
        this.code = code;
        this.name = name;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<StockItem> getStockItems() {
        return stockItems;
    }

    public void setStockItems(List<StockItem> stockItems) {
        this.stockItems = stockItems;
    }

    public String getManagerName() {
        return "";
    }

    public void setManagerName(String trim) {
    }

    public void setManagerPhone(String trim) {
    }

    public void setManagerEmail(String trim) {
    }

    public void setDescription(String trim) {
    }

    public String getManagerPhone() {
        return "";
    }

    public String getManagerEmail() {
        return "";
    }

    public String getDescription() {
        return "";
    }
}

