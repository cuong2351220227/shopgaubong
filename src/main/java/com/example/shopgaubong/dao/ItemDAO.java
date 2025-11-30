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
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT i FROM Item i JOIN FETCH i.category WHERE i.sku = :sku";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setParameter("sku", sku);
            List<Item> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        }
    }

    /**
     * Tìm các Item theo danh mục
     */
    public List<Item> findByCategoryId(Long categoryId) {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT i FROM Item i JOIN FETCH i.category WHERE i.category.id = :categoryId AND i.isActive = true";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setParameter("categoryId", categoryId);
            return query.getResultList();
        }
    }

    /**
     * Tìm các Item theo danh mục (alias method)
     */
    public List<Item> findByCategory(Long categoryId) {
        return findByCategoryId(categoryId);
    }

    /**
     * Tìm các Item theo trạng thái active
     */
    public List<Item> findByActive(Boolean isActive) {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT i FROM Item i JOIN FETCH i.category WHERE i.isActive = :isActive ORDER BY i.name";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setParameter("isActive", isActive);
            return query.getResultList();
        }
    }

    /**
     * Tìm các Item đang hoạt động
     */
    public List<Item> findActiveItems() {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT i FROM Item i JOIN FETCH i.category WHERE i.isActive = true ORDER BY i.name";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            return query.getResultList();
        }
    }

    /**
     * Tìm kiếm Item theo tên hoặc SKU
     */
    public List<Item> search(String keyword) {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT i FROM Item i JOIN FETCH i.category WHERE (LOWER(i.name) LIKE LOWER(:keyword) OR LOWER(i.sku) LIKE LOWER(:keyword)) AND i.isActive = true ORDER BY i.name";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.getResultList();
        }
    }

    /**
     * Kiểm tra SKU đã tồn tại chưa
     */
    public boolean existsBySku(String sku) {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT COUNT(i) FROM Item i WHERE i.sku = :sku";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("sku", sku);
            return query.getSingleResult() > 0;
        }
    }

    /**
     * Lấy top sản phẩm bán chạy
     */
    public List<Item> findTopSellingItems(int limit) {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT oi.item, SUM(oi.quantity) as totalQty " +
                    "FROM OrderItem oi " +
                    "JOIN FETCH oi.item i " +
                    "JOIN FETCH i.category " +
                    "JOIN oi.order o " +
                    "WHERE o.status IN ('DELIVERED', 'CLOSED') " +
                    "GROUP BY oi.item " +
                    "ORDER BY totalQty DESC";
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setMaxResults(limit);
            return query.getResultList().stream()
                    .map(result -> (Item) result[0])
                    .toList();
        }
    }

    /**
     * Override findAll để thêm JOIN FETCH cho category
     */
    @Override
    public List<Item> findAll() {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT i FROM Item i JOIN FETCH i.category ORDER BY i.name";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            return query.getResultList();
        }
    }

    /**
     * Override findById để thêm JOIN FETCH cho category
     */
    @Override
    public Optional<Item> findById(Long id) {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT i FROM Item i JOIN FETCH i.category WHERE i.id = :id";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setParameter("id", id);
            List<Item> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
        }
    }

    /**
     * Tìm các Item với phân trang và sắp xếp
     */
    public List<Item> findWithPagination(int page, int size, String sortBy) {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT i FROM Item i JOIN FETCH i.category WHERE i.isActive = true ORDER BY " +
                    (sortBy != null ? "i." + sortBy : "i.name");
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        }
    }

    /**
     * Đếm tổng số Item active
     */
    public Long countActiveItems() {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT COUNT(i) FROM Item i WHERE i.isActive = true";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            return query.getSingleResult();
        }
    }

    /**
     * Tìm Item theo danh mục với phân trang
     */
    public List<Item> findByCategoryWithPagination(Long categoryId, int page, int size) {
        try (EntityManager em = getEntityManager()) {
            String jpql = "SELECT i FROM Item i JOIN FETCH i.category c WHERE c.id = :categoryId AND i.isActive = true ORDER BY i.name";
            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setParameter("categoryId", categoryId);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        }
    }
}
