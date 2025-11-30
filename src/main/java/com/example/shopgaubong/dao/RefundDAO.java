package com.example.shopgaubong.dao;

import com.example.shopgaubong.entity.Refund;
import com.example.shopgaubong.enums.RefundStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class RefundDAO extends BaseDAO<Refund, Long> {

    public RefundDAO() {
        super(Refund.class);
    }

    /**
     * Tìm các Refund theo Payment
     */
    public List<Refund> findByPaymentId(Long paymentId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT r FROM Refund r WHERE r.payment.id = :paymentId ORDER BY r.createdAt DESC";
            TypedQuery<Refund> query = em.createQuery(jpql, Refund.class);
            query.setParameter("paymentId", paymentId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Refund theo Order
     */
    public List<Refund> findByOrderId(Long orderId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT r FROM Refund r WHERE r.payment.order.id = :orderId ORDER BY r.createdAt DESC";
            TypedQuery<Refund> query = em.createQuery(jpql, Refund.class);
            query.setParameter("orderId", orderId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Refund theo trạng thái
     */
    public List<Refund> findByStatus(RefundStatus status) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT r FROM Refund r WHERE r.status = :status ORDER BY r.createdAt DESC";
            TypedQuery<Refund> query = em.createQuery(jpql, Refund.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Refund đang chờ duyệt
     */
    public List<Refund> findPendingRefunds() {
        return findByStatus(RefundStatus.PENDING);
    }

    /**
     * Tìm Refund theo số refund
     */
    public Refund findByRefundNumber(String refundNumber) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT r FROM Refund r WHERE r.refundNumber = :refundNumber";
            TypedQuery<Refund> query = em.createQuery(jpql, Refund.class);
            query.setParameter("refundNumber", refundNumber);
            List<Refund> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
}

