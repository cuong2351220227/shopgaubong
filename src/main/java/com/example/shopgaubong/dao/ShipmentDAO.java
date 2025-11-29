package com.example.shopgaubong.dao;

import com.example.shopgaubong.entity.Shipment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class ShipmentDAO extends BaseDAO<Shipment, Long> {

    public ShipmentDAO() {
        super(Shipment.class);
    }

    /**
     * Tìm Shipment theo tracking number
     */
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT s FROM Shipment s WHERE s.trackingNumber = :trackingNumber";
            TypedQuery<Shipment> query = em.createQuery(jpql, Shipment.class);
            query.setParameter("trackingNumber", trackingNumber);
            List<Shipment> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Tìm các Shipment theo Order
     */
    public List<Shipment> findByOrderId(Long orderId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT s FROM Shipment s WHERE s.order.id = :orderId ORDER BY s.createdAt DESC";
            TypedQuery<Shipment> query = em.createQuery(jpql, Shipment.class);
            query.setParameter("orderId", orderId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}

