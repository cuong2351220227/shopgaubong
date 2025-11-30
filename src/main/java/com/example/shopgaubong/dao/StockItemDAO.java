package com.example.shopgaubong.dao;

import com.example.shopgaubong.entity.StockItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class StockItemDAO extends BaseDAO<StockItem, Long> {

    public StockItemDAO() {
        super(StockItem.class);
    }

    /**
     * Tìm StockItem theo warehouse và item
     */
    public Optional<StockItem> findByWarehouseAndItem(Long warehouseId, Long itemId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT s FROM StockItem s JOIN FETCH s.warehouse JOIN FETCH s.item WHERE s.warehouse.id = :warehouseId AND s.item.id = :itemId";
            TypedQuery<StockItem> query = em.createQuery(jpql, StockItem.class);
            query.setParameter("warehouseId", warehouseId);
            query.setParameter("itemId", itemId);
            List<StockItem> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các StockItem theo warehouse
     */
    public List<StockItem> findByWarehouseId(Long warehouseId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT s FROM StockItem s JOIN FETCH s.warehouse JOIN FETCH s.item WHERE s.warehouse.id = :warehouseId";
            TypedQuery<StockItem> query = em.createQuery(jpql, StockItem.class);
            query.setParameter("warehouseId", warehouseId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Alias method for findByWarehouseId
     */
    public List<StockItem> findByWarehouse(Long warehouseId) {
        return findByWarehouseId(warehouseId);
    }

    /**
     * Tìm các StockItem theo item
     */
    public List<StockItem> findByItemId(Long itemId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT s FROM StockItem s JOIN FETCH s.warehouse JOIN FETCH s.item WHERE s.item.id = :itemId";
            TypedQuery<StockItem> query = em.createQuery(jpql, StockItem.class);
            query.setParameter("itemId", itemId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Alias method for findByItemId
     */
    public List<StockItem> findByItem(Long itemId) {
        return findByItemId(itemId);
    }

    /**
     * Tìm các StockItem có tồn kho thấp
     */
    public List<StockItem> findLowStockItems() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT s FROM StockItem s JOIN FETCH s.warehouse JOIN FETCH s.item WHERE (s.onHand - s.reserved) <= s.lowStockThreshold";
            TypedQuery<StockItem> query = em.createQuery(jpql, StockItem.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Alias method for findLowStockItems
     */
    public List<StockItem> findLowStock() {
        return findLowStockItems();
    }

    /**
     * Tính tổng tồn kho của một item (tất cả warehouses)
     */
    public Integer getTotalStockByItem(Long itemId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT SUM(s.onHand - s.reserved) FROM StockItem s WHERE s.item.id = :itemId";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("itemId", itemId);
            Long result = query.getSingleResult();
            return result != null ? result.intValue() : 0;
        } finally {
            em.close();
        }
    }
}
