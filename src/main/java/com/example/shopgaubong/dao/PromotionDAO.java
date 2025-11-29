package com.example.shopgaubong.dao;

import com.example.shopgaubong.entity.Promotion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PromotionDAO extends BaseDAO<Promotion, Long> {

    public PromotionDAO() {
        super(Promotion.class);
    }

    /**
     * Tìm Promotion theo mã
     */
    public Optional<Promotion> findByCode(String code) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT p FROM Promotion p WHERE p.code = :code";
            TypedQuery<Promotion> query = em.createQuery(jpql, Promotion.class);
            query.setParameter("code", code);
            List<Promotion> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Promotion đang hoạt động
     */
    public List<Promotion> findActivePromotions() {
        EntityManager em = getEntityManager();
        try {
            LocalDateTime now = LocalDateTime.now();
            String jpql = "SELECT p FROM Promotion p WHERE p.isActive = true AND p.startDate <= :now AND p.endDate >= :now";
            TypedQuery<Promotion> query = em.createQuery(jpql, Promotion.class);
            query.setParameter("now", now);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm Promotion hợp lệ theo mã
     */
    public Optional<Promotion> findValidPromotionByCode(String code) {
        EntityManager em = getEntityManager();
        try {
            LocalDateTime now = LocalDateTime.now();
            String jpql = "SELECT p FROM Promotion p WHERE p.code = :code AND p.isActive = true " +
                    "AND p.startDate <= :now AND p.endDate >= :now " +
                    "AND (p.maxUsage IS NULL OR p.currentUsage < p.maxUsage)";
            TypedQuery<Promotion> query = em.createQuery(jpql, Promotion.class);
            query.setParameter("code", code);
            query.setParameter("now", now);
            List<Promotion> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra mã khuyến mãi đã tồn tại chưa
     */
    public boolean existsByCode(String code) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(p) FROM Promotion p WHERE p.code = :code";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("code", code);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }
}

