package com.example.shopgaubong.dao;

import com.example.shopgaubong.entity.Payment;
import com.example.shopgaubong.enums.PaymentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class PaymentDAO extends BaseDAO<Payment, Long> {

    public PaymentDAO() {
        super(Payment.class);
    }

    /**
     * Tìm các Payment theo Order
     */
    public List<Payment> findByOrderId(Long orderId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT p FROM Payment p WHERE p.order.id = :orderId";
            TypedQuery<Payment> query = em.createQuery(jpql, Payment.class);
            query.setParameter("orderId", orderId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Payment đã thanh toán theo Order
     */
    public List<Payment> findPaidByOrderId(Long orderId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.isPaid = true";
            TypedQuery<Payment> query = em.createQuery(jpql, Payment.class);
            query.setParameter("orderId", orderId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm Payment theo transaction ID
     */
    public Payment findByTransactionId(String transactionId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT p FROM Payment p WHERE p.transactionId = :transactionId";
            TypedQuery<Payment> query = em.createQuery(jpql, Payment.class);
            query.setParameter("transactionId", transactionId);
            List<Payment> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Payment theo trạng thái
     */
    public List<Payment> findByStatus(PaymentStatus status) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.createdAt DESC";
            TypedQuery<Payment> query = em.createQuery(jpql, Payment.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}

