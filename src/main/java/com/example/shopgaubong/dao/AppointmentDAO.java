package com.example.shopgaubong.dao;

import com.example.shopgaubong.entity.Appointment;
import com.example.shopgaubong.enums.AppointmentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AppointmentDAO extends BaseDAO<Appointment, Long> {

    public AppointmentDAO() {
        super(Appointment.class);
    }

    /**
     * Tìm appointment theo mã
     */
    public Optional<Appointment> findByCode(String appointmentCode) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Appointment a WHERE a.appointmentCode = :code";
            TypedQuery<Appointment> query = em.createQuery(jpql, Appointment.class);
            query.setParameter("code", appointmentCode);
            List<Appointment> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả appointment của một đơn hàng
     */
    public List<Appointment> findByOrderId(Long orderId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Appointment a WHERE a.order.id = :orderId ORDER BY a.appointmentTime DESC";
            TypedQuery<Appointment> query = em.createQuery(jpql, Appointment.class);
            query.setParameter("orderId", orderId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy appointment theo trạng thái
     */
    public List<Appointment> findByStatus(AppointmentStatus status) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Appointment a WHERE a.status = :status ORDER BY a.appointmentTime ASC";
            TypedQuery<Appointment> query = em.createQuery(jpql, Appointment.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy appointment theo nhân viên
     */
    public List<Appointment> findByStaff(String staffName) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Appointment a WHERE a.assignedStaff = :staff ORDER BY a.appointmentTime ASC";
            TypedQuery<Appointment> query = em.createQuery(jpql, Appointment.class);
            query.setParameter("staff", staffName);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy appointment trong khoảng thời gian
     */
    public List<Appointment> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Appointment a WHERE a.appointmentTime BETWEEN :start AND :end ORDER BY a.appointmentTime ASC";
            TypedQuery<Appointment> query = em.createQuery(jpql, Appointment.class);
            query.setParameter("start", startTime);
            query.setParameter("end", endTime);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy appointment sắp tới (đã xác nhận)
     */
    public List<Appointment> findUpcoming() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Appointment a WHERE a.status = :status AND a.appointmentTime >= :now ORDER BY a.appointmentTime ASC";
            TypedQuery<Appointment> query = em.createQuery(jpql, Appointment.class);
            query.setParameter("status", AppointmentStatus.CONFIRMED);
            query.setParameter("now", LocalDateTime.now());
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy appointment theo số điện thoại khách hàng
     */
    public List<Appointment> findByCustomerPhone(String phone) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT a FROM Appointment a WHERE a.customerPhone = :phone ORDER BY a.appointmentTime DESC";
            TypedQuery<Appointment> query = em.createQuery(jpql, Appointment.class);
            query.setParameter("phone", phone);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
