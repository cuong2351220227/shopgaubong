package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.AppointmentDAO;
import com.example.shopgaubong.entity.Appointment;
import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.enums.AppointmentStatus;
import com.example.shopgaubong.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý lịch hẹn
 */
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);
    private final AppointmentDAO appointmentDAO;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
    }

    /**
     * Tạo appointment mới
     */
    public Appointment createAppointment(Order order, LocalDateTime appointmentTime,
                                        String location, String customerName, String customerPhone,
                                        String serviceDescription) {
        
        // Validate appointment time (must be in future)
        if (appointmentTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hẹn phải sau thời điểm hiện tại");
        }
        
        // Generate appointment code
        String appointmentCode = generateAppointmentCode(appointmentTime);
        
        Appointment appointment = new Appointment(order, appointmentCode, appointmentTime);
        appointment.setLocation(location);
        appointment.setCustomerName(customerName);
        appointment.setCustomerPhone(customerPhone);
        appointment.setServiceDescription(serviceDescription);
        
        // Set default end time (1 hour later)
        appointment.setEndTime(appointmentTime.plusHours(1));
        
        String currentUser = SessionManager.getInstance().getCurrentUsername();
        appointment.setCreatedBy(currentUser);
        appointment.setUpdatedBy(currentUser);
        
        Appointment saved = appointmentDAO.save(appointment);
        logger.info("Tạo lịch hẹn mới: {} lúc {}", appointmentCode, appointmentTime);
        
        return saved;
    }

    /**
     * Xác nhận lịch hẹn
     */
    public void confirmAppointment(Long appointmentId, String assignedStaff) {
        Appointment appointment = appointmentDAO.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch hẹn không tồn tại"));
        
        appointment.confirm();
        appointment.setAssignedStaff(assignedStaff);
        
        String currentUser = SessionManager.getInstance().getCurrentUsername();
        appointment.setUpdatedBy(currentUser);
        
        appointmentDAO.update(appointment);
        logger.info("Xác nhận lịch hẹn {} - Nhân viên: {}", 
            appointment.getAppointmentCode(), assignedStaff);
    }

    /**
     * Bắt đầu thực hiện
     */
    public void startAppointment(Long appointmentId) {
        Appointment appointment = appointmentDAO.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch hẹn không tồn tại"));
        
        appointment.start();
        
        String currentUser = SessionManager.getInstance().getCurrentUsername();
        appointment.setUpdatedBy(currentUser);
        
        appointmentDAO.update(appointment);
        logger.info("Bắt đầu thực hiện lịch hẹn {}", appointment.getAppointmentCode());
    }

    /**
     * Hoàn thành lịch hẹn
     */
    public void completeAppointment(Long appointmentId, String notes) {
        Appointment appointment = appointmentDAO.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch hẹn không tồn tại"));
        
        appointment.complete();
        if (notes != null && !notes.isEmpty()) {
            appointment.setNotes(notes);
        }
        
        String currentUser = SessionManager.getInstance().getCurrentUsername();
        appointment.setUpdatedBy(currentUser);
        
        appointmentDAO.update(appointment);
        logger.info("Hoàn thành lịch hẹn {}", appointment.getAppointmentCode());
    }

    /**
     * Hủy lịch hẹn
     */
    public void cancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = appointmentDAO.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch hẹn không tồn tại"));
        
        appointment.cancel(reason);
        
        String currentUser = SessionManager.getInstance().getCurrentUsername();
        appointment.setUpdatedBy(currentUser);
        
        appointmentDAO.update(appointment);
        logger.info("Hủy lịch hẹn {}: {}", appointment.getAppointmentCode(), reason);
    }

    /**
     * Đánh dấu không đến
     */
    public void markNoShow(Long appointmentId) {
        Appointment appointment = appointmentDAO.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch hẹn không tồn tại"));
        
        appointment.markNoShow();
        
        String currentUser = SessionManager.getInstance().getCurrentUsername();
        appointment.setUpdatedBy(currentUser);
        
        appointmentDAO.update(appointment);
        logger.info("Đánh dấu không đến: {}", appointment.getAppointmentCode());
    }

    /**
     * Đổi lịch hẹn
     */
    public void rescheduleAppointment(Long appointmentId, LocalDateTime newTime) {
        Appointment appointment = appointmentDAO.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch hẹn không tồn tại"));
        
        if (!appointment.canReschedule()) {
            throw new IllegalStateException("Không thể đổi lịch hẹn ở trạng thái hiện tại");
        }
        
        if (newTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hẹn mới phải sau thời điểm hiện tại");
        }
        
        appointment.setAppointmentTime(newTime);
        appointment.setEndTime(newTime.plusHours(1));
        
        String currentUser = SessionManager.getInstance().getCurrentUsername();
        appointment.setUpdatedBy(currentUser);
        
        appointmentDAO.update(appointment);
        logger.info("Đổi lịch hẹn {} sang {}", appointment.getAppointmentCode(), newTime);
    }

    /**
     * Lấy appointment theo mã
     */
    public Optional<Appointment> getByCode(String appointmentCode) {
        return appointmentDAO.findByCode(appointmentCode);
    }

    /**
     * Lấy appointment của đơn hàng
     */
    public List<Appointment> getByOrderId(Long orderId) {
        return appointmentDAO.findByOrderId(orderId);
    }

    /**
     * Lấy lịch hẹn sắp tới
     */
    public List<Appointment> getUpcomingAppointments() {
        return appointmentDAO.findUpcoming();
    }

    /**
     * Lấy lịch hẹn theo trạng thái
     */
    public List<Appointment> getByStatus(AppointmentStatus status) {
        return appointmentDAO.findByStatus(status);
    }

    /**
     * Lấy lịch hẹn theo nhân viên
     */
    public List<Appointment> getByStaff(String staffName) {
        return appointmentDAO.findByStaff(staffName);
    }

    /**
     * Lấy lịch hẹn trong khoảng thời gian
     */
    public List<Appointment> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return appointmentDAO.findByTimeRange(startTime, endTime);
    }

    /**
     * Generate appointment code
     */
    private String generateAppointmentCode(LocalDateTime appointmentTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = appointmentTime.format(formatter);
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "APT-" + dateStr + "-" + timestamp.substring(timestamp.length() - 6);
    }
}
