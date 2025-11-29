package com.example.shopgaubong.dao;

import com.example.shopgaubong.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public abstract class BaseDAO<T, ID> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final Class<T> entityClass;

    protected BaseDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }

    /**
     * Tìm entity theo ID
     */
    public Optional<T> findById(ID id) {
        EntityManager em = getEntityManager();
        try {
            T entity = em.find(entityClass, id);
            return Optional.ofNullable(entity);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả entities
     */
    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lưu entity mới
     */
    public T save(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            em.persist(entity);
            tx.commit();
            logger.info("Đã lưu entity: {}", entity);
            return entity;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Lỗi khi lưu entity", e);
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật entity
     */
    public T update(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            logger.info("Đã cập nhật entity: {}", merged);
            return merged;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Lỗi khi cập nhật entity", e);
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Xóa entity theo ID
     */
    public void deleteById(ID id) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            tx.commit();
            logger.info("Đã xóa entity với ID: {}", id);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Lỗi khi xóa entity", e);
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Xóa entity
     */
    public void delete(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            em.remove(em.contains(entity) ? entity : em.merge(entity));
            tx.commit();
            logger.info("Đã xóa entity: {}", entity);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Lỗi khi xóa entity", e);
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Đếm số lượng entities
     */
    public long count() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra entity có tồn tại không
     */
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
}

