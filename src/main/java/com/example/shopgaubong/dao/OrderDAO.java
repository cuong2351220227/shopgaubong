package com.example.shopgaubong.dao;

import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.enums.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OrderDAO extends BaseDAO<Order, Long> {

    public OrderDAO() {
        super(Order.class);
    }

    /**
     * Tìm Order theo orderNumber
     */
    public Optional<Order> findByOrderNumber(String orderNumber) {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "WHERE o.orderNumber = :orderNumber";
            TypedQuery<Order> query = em.createQuery(jpql, Order.class);
            query.setParameter("orderNumber", orderNumber);
            List<Order> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        }
    }

    /**
     * Tìm các Order theo customer
     */
    public List<Order> findByCustomerId(Long customerId) {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "WHERE o.customer.id = :customerId " +
                    "ORDER BY o.createdAt DESC";
            TypedQuery<Order> query = em.createQuery(jpql, Order.class);
            query.setParameter("customerId", customerId);
            return query.getResultList();
        }
    }

    /**
     * Tìm các Order theo trạng thái
     */
    public List<Order> findByStatus(OrderStatus status) {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "WHERE o.status = :status " +
                    "ORDER BY o.createdAt DESC";
            TypedQuery<Order> query = em.createQuery(jpql, Order.class);
            query.setParameter("status", status);
            return query.getResultList();
        }
    }

    /**
     * Tìm Order giỏ hàng hiện tại của customer
     */
    public Optional<Order> findCartByCustomerId(Long customerId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "WHERE o.customer.id = :customerId AND o.status = :status";
            TypedQuery<Order> query = em.createQuery(jpql, Order.class);
            query.setParameter("customerId", customerId);
            query.setParameter("status", OrderStatus.CART);
            List<Order> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Order theo khoảng thời gian
     */
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
                    "ORDER BY o.createdAt DESC";
            TypedQuery<Order> query = em.createQuery(jpql, Order.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Order theo trạng thái và khoảng thời gian
     */
    public List<Order> findByStatusAndDateRange(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = getEntityManager();
        try {
            // Đã thêm LEFT JOIN FETCH và DISTINCT để tránh lỗi Lazy Loading
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "WHERE o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate " +
                    "ORDER BY o.createdAt DESC";
            TypedQuery<Order> query = em.createQuery(jpql, Order.class);
            query.setParameter("status", status);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Order theo customer và trạng thái
     */
    public List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "WHERE o.customer.id = :customerId AND o.status = :status " +
                    "ORDER BY o.createdAt DESC";
            TypedQuery<Order> query = em.createQuery(jpql, Order.class);
            query.setParameter("customerId", customerId);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Override findAll để có eager loading
     */
    @Override
    public List<Order> findAll() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "LEFT JOIN FETCH o.promotion " +
                    "ORDER BY o.createdAt DESC";
            return em.createQuery(jpql, Order.class).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Override findById để có eager loading
     */
    @Override
    public Optional<Order> findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH c.profile " +
                    "LEFT JOIN FETCH o.orderItems oi " +
                    "LEFT JOIN FETCH oi.item i " +
                    "LEFT JOIN FETCH i.category " +
                    "LEFT JOIN FETCH o.promotion " +
                    "WHERE o.id = :id";
            var query = em.createQuery(jpql, Order.class);
            query.setParameter("id", id);
            var results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }
}