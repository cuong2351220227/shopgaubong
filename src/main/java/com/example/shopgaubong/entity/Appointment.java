package com.example.shopgaubong.entity;

import com.example.shopgaubong.entity.base.AuditEntity;
import com.example.shopgaubong.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entity quản lý lịch hẹn cho dịch vụ
 */
@Entity
@Table(name = "appointments")
public class Appointment extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Đơn hàng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Mã lịch hẹn không được để trống")
    @Size(min = 5, max = 50, message = "Mã lịch hẹn phải từ 5-50 ký tự")
    @Column(unique = true, nullable = false, length = 50)
    private String appointmentCode;

    @NotNull(message = "Trạng thái không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppointmentStatus status;

    @NotNull(message = "Thời gian hẹn không được để trống")
    @Column(nullable = false)
    private LocalDateTime appointmentTime;

    @Column
    private LocalDateTime endTime; // Thời gian kết thúc dự kiến

    @Size(max = 500, message = "Địa điểm tối đa 500 ký tự")
    @Column(length = 500)
    private String location; // Địa điểm thực hiện dịch vụ

    @Size(max = 200, message = "Tên khách hàng tối đa 200 ký tự")
    @Column(length = 200)
    private String customerName;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    @Column(length = 20)
    private String customerPhone;

    @Size(max = 100, message = "Tên nhân viên tối đa 100 ký ttự")
    @Column(length = 100)
    private String assignedStaff; // Nhân viên được phân công

    @Column(columnDefinition = "TEXT")
    private String serviceDescription; // Mô tả dịch vụ

    @Column(columnDefinition = "TEXT")
    private String notes; // Ghi chú

    @Column(columnDefinition = "TEXT")
    private String cancellationReason; // Lý do hủy (nếu có)

    @Column
    private LocalDateTime completedTime; // Thời gian hoàn thành thực tế

    @Column
    private LocalDateTime confirmedTime; // Thời gian xác nhận

    // Constructors
    public Appointment() {
        this.status = AppointmentStatus.PENDING;
    }

    public Appointment(Order order, String appointmentCode, LocalDateTime appointmentTime) {
        this();
        this.order = order;
        this.appointmentCode = appointmentCode;
        this.appointmentTime = appointmentTime;
    }

    // Business methods
    public void confirm() {
        if (this.status != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể xác nhận lịch hẹn ở trạng thái chờ xác nhận");
        }
        this.status = AppointmentStatus.CONFIRMED;
        this.confirmedTime = LocalDateTime.now();
    }

    public void start() {
        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể bắt đầu lịch hẹn đã được xác nhận");
        }
        this.status = AppointmentStatus.IN_PROGRESS;
    }

    public void complete() {
        if (this.status != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Chỉ có thể hoàn thành lịch hẹn đang thực hiện");
        }
        this.status = AppointmentStatus.COMPLETED;
        this.completedTime = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Không thể hủy lịch hẹn đã hoàn thành");
        }
        this.status = AppointmentStatus.CANCELLED;
        this.cancellationReason = reason;
    }

    public void markNoShow() {
        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể đánh dấu không đến cho lịch hẹn đã xác nhận");
        }
        this.status = AppointmentStatus.NO_SHOW;
    }

    public boolean canReschedule() {
        return status == AppointmentStatus.PENDING || status == AppointmentStatus.CONFIRMED;
    }

    public boolean isPast() {
        return appointmentTime.isBefore(LocalDateTime.now());
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

    public String getAppointmentCode() {
        return appointmentCode;
    }

    public void setAppointmentCode(String appointmentCode) {
        this.appointmentCode = appointmentCode;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(String assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }

    public LocalDateTime getConfirmedTime() {
        return confirmedTime;
    }

    public void setConfirmedTime(LocalDateTime confirmedTime) {
        this.confirmedTime = confirmedTime;
    }
}
