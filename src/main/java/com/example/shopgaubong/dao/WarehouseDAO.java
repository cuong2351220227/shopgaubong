package com.example.shopgaubong.dao;

import com.example.shopgaubong.entity.Warehouse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class WarehouseDAO extends BaseDAO<Warehouse, Long> {

    public WarehouseDAO() {
        super(Warehouse.class);
    }

    /**
     * Tìm Warehouse theo mã
     */
    public Optional<Warehouse> findByCode(String code) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT w FROM Warehouse w WHERE w.code = :code";
            TypedQuery<Warehouse> query = em.createQuery(jpql, Warehouse.class);
            query.setParameter("code", code);
            List<Warehouse> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Warehouse đang hoạt động
     */
    public List<Warehouse> findActiveWarehouses() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT w FROM Warehouse w WHERE w.isActive = true ORDER BY w.name";
            TypedQuery<Warehouse> query = em.createQuery(jpql, Warehouse.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Alias method - find by active status
     */
    public List<Warehouse> findByActive(Boolean isActive) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT w FROM Warehouse w WHERE w.isActive = :isActive ORDER BY w.name";
            TypedQuery<Warehouse> query = em.createQuery(jpql, Warehouse.class);
            query.setParameter("isActive", isActive);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra mã kho đã tồn tại chưa
     */
    public boolean existsByCode(String code) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(w) FROM Warehouse w WHERE w.code = :code";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("code", code);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }
}

