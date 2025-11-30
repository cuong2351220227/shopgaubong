package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.WarehouseDAO;
import com.example.shopgaubong.entity.Warehouse;
import com.example.shopgaubong.util.HibernateUtil;
import com.example.shopgaubong.util.SessionManager;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class WarehouseService {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseService.class);
    private final WarehouseDAO warehouseDAO;

    public WarehouseService() {
        this.warehouseDAO = new WarehouseDAO();
    }

    /**
     * Tạo kho mới
     */
    public Warehouse createWarehouse(String code, String name, String address, String city,
                                     String district, String ward, String phone) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Check if code already exists
            if (warehouseDAO.existsByCode(code)) {
                throw new IllegalArgumentException("Mã kho đã tồn tại: " + code);
            }

            Warehouse warehouse = new Warehouse();
            warehouse.setCode(code);
            warehouse.setName(name);
            warehouse.setAddress(address);
            warehouse.setCity(city);
            warehouse.setDistrict(district);
            warehouse.setWard(ward);
            warehouse.setPhone(phone);
            warehouse.setIsActive(true);

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            warehouse.setCreatedBy(currentUser);
            warehouse.setUpdatedBy(currentUser);

            Warehouse saved = warehouseDAO.save(warehouse);
            em.getTransaction().commit();

            logger.info("Tạo kho mới: {} - {}", saved.getCode(), saved.getName());
            return saved;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Lỗi khi tạo kho: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Tạo kho mới với đầy đủ thông tin
     */
    public Warehouse createWarehouse(Warehouse warehouse) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Check if code already exists
            if (warehouseDAO.existsByCode(warehouse.getCode())) {
                throw new IllegalArgumentException("Mã kho đã tồn tại: " + warehouse.getCode());
            }

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            warehouse.setCreatedBy(currentUser);
            warehouse.setUpdatedBy(currentUser);

            Warehouse saved = warehouseDAO.save(warehouse);
            em.getTransaction().commit();

            logger.info("Tạo kho mới: {} - {}", saved.getCode(), saved.getName());
            return saved;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Lỗi khi tạo kho: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật thông tin kho
     */
    public Warehouse updateWarehouse(Long id, String code, String name, String address,
                                     String city, String district, String ward, String phone, Boolean isActive) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Warehouse warehouse = warehouseDAO.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Kho không tồn tại"));

            // Check if code changed and already exists
            if (!warehouse.getCode().equals(code) && warehouseDAO.existsByCode(code)) {
                throw new IllegalArgumentException("Mã kho đã tồn tại: " + code);
            }

            warehouse.setCode(code);
            warehouse.setName(name);
            warehouse.setAddress(address);
            warehouse.setCity(city);
            warehouse.setDistrict(district);
            warehouse.setWard(ward);
            warehouse.setPhone(phone);
            warehouse.setIsActive(isActive);

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            warehouse.setUpdatedBy(currentUser);

            Warehouse updated = warehouseDAO.update(warehouse);
            em.getTransaction().commit();

            logger.info("Cập nhật kho: {} - {}", updated.getCode(), updated.getName());
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Lỗi khi cập nhật kho: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật thông tin kho
     */
    public Warehouse updateWarehouse(Warehouse warehouse) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            warehouse.setUpdatedBy(currentUser);

            Warehouse updated = warehouseDAO.update(warehouse);
            em.getTransaction().commit();

            logger.info("Cập nhật kho: {} - {}", updated.getCode(), updated.getName());
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Lỗi khi cập nhật kho: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Xóa kho (soft delete - set inactive)
     */
    public void deleteWarehouse(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Warehouse warehouse = warehouseDAO.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Kho không tồn tại"));

            warehouse.setIsActive(false);

            String currentUser = SessionManager.getInstance().getCurrentUsername();
            warehouse.setUpdatedBy(currentUser);

            warehouseDAO.update(warehouse);
            em.getTransaction().commit();

            logger.info("Xóa kho: {} - {}", warehouse.getCode(), warehouse.getName());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Lỗi khi xóa kho: {}", e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả kho
     */
    public List<Warehouse> getAllWarehouses() {
        return warehouseDAO.findAll();
    }

    /**
     * Lấy các kho đang hoạt động
     */
    public List<Warehouse> getActiveWarehouses() {
        return warehouseDAO.findActiveWarehouses();
    }

    /**
     * Lấy kho theo ID
     */
    public Optional<Warehouse> getWarehouseById(Long id) {
        return warehouseDAO.findById(id);
    }

    /**
     * Lấy kho theo mã
     */
    public Optional<Warehouse> getWarehouseByCode(String code) {
        return warehouseDAO.findByCode(code);
    }

    /**
     * Kiểm tra mã kho đã tồn tại
     */
    public boolean isCodeExists(String code) {
        return warehouseDAO.existsByCode(code);
    }
}

