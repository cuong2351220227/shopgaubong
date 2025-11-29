package com.example.shopgaubong.dao;

import com.example.shopgaubong.entity.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class ItemDAO extends BaseDAO<Item, Long> {

    public ItemDAO() {
        super(Item.class);
    }

    /**
     * Tìm Item theo SKU
     */
    public Optional<Item> findBySku(String sku) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT i FROM Item i WHERE i.sku = :sku";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setParameter("sku", sku);
            List<Item> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Item theo danh mục
     */
    public List<Item> findByCategoryId(Long categoryId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT i FROM Item i WHERE i.category.id = :categoryId AND i.isActive = true";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setParameter("categoryId", categoryId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Item đang hoạt động
     */
    public List<Item> findActiveItems() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT i FROM Item i WHERE i.isActive = true ORDER BY i.name";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm kiếm Item theo tên hoặc SKU
     */
    public List<Item> search(String keyword) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT i FROM Item i WHERE (LOWER(i.name) LIKE LOWER(:keyword) OR LOWER(i.sku) LIKE LOWER(:keyword)) AND i.isActive = true";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra SKU đã tồn tại chưa
     */
    public boolean existsBySku(String sku) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(i) FROM Item i WHERE i.sku = :sku";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("sku", sku);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy top sản phẩm bán chạy
     */
    public List<Item> findTopSellingItems(int limit) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT oi.item, SUM(oi.quantity) as totalQty " +
                    "FROM OrderItem oi " +
                    "JOIN oi.order o " +
                    "WHERE o.status IN ('DELIVERED', 'CLOSED') " +
                    "GROUP BY oi.item " +
                    "ORDER BY totalQty DESC";
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setMaxResults(limit);
            return query.getResultList().stream()
                    .map(result -> (Item) result[0])
                    .toList();
        } finally {
            em.close();
        }
    }
}

