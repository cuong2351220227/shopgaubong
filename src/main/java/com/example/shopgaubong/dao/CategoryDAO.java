package com.example.shopgaubong.dao;

import com.example.shopgaubong.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class CategoryDAO extends BaseDAO<Category, Long> {

    public CategoryDAO() {
        super(Category.class);
    }

    /**
     * Tìm các danh mục gốc (không có parent)
     */
    public List<Category> findRootCategories() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các danh mục con theo parent ID
     */
    public List<Category> findByParentId(Long parentId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            query.setParameter("parentId", parentId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các danh mục đang hoạt động
     */
    public List<Category> findActiveCategories() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.name";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm kiếm danh mục theo tên
     */
    public List<Category> searchByName(String name) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(:name) AND c.isActive = true";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}

