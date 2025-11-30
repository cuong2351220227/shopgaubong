package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.OrderDAO;
import com.example.shopgaubong.dao.ShipmentDAO;
import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.entity.Shipment;
import com.example.shopgaubong.enums.ShipmentStatus;
import com.example.shopgaubong.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ShipmentService {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentService.class);
    private final ShipmentDAO shipmentDAO;
    private final OrderDAO orderDAO;

    public ShipmentService() {
        this.shipmentDAO = new ShipmentDAO();
        this.orderDAO = new OrderDAO();
    }

    /**
     * Tạo vận đơn mới
     */
    public Shipment createShipment(Long orderId, String trackingNumber,
                                   String carrier, LocalDateTime estimatedDeliveryAt) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setCarrier(carrier);
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setEstimatedDeliveryAt(estimatedDeliveryAt);

        String currentUser = SessionManager.getInstance().getCurrentUsername();
        shipment.setCreatedBy(currentUser);
        shipment.setUpdatedBy(currentUser);

        Shipment saved = shipmentDAO.save(shipment);
        logger.info("Tạo vận đơn mới: {} cho đơn hàng {}",
                trackingNumber, order.getOrderNumber());
        return saved;
    }

    /**
     * Cập nhật trạng thái vận đơn
     */
    public Shipment updateShipmentStatus(Long shipmentId, ShipmentStatus newStatus,
                                        String note, LocalDateTime deliveredAt) {
        Shipment shipment = shipmentDAO.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Vận đơn không tồn tại"));

        shipment.setStatus(newStatus);
        if (note != null && !note.isEmpty()) {
            shipment.setNotes(shipment.getNotes() != null ?
                    shipment.getNotes() + "\n" + note : note);
        }

        if (newStatus == ShipmentStatus.DELIVERED && deliveredAt != null) {
            shipment.setDeliveredAt(deliveredAt);
        }

        String currentUser = SessionManager.getInstance().getCurrentUsername();
        shipment.setUpdatedBy(currentUser);

        Shipment updated = shipmentDAO.update(shipment);
        logger.info("Cập nhật trạng thái vận đơn {} thành {}",
                shipment.getTrackingNumber(), newStatus);
        return updated;
    }

    /**
     * Cập nhật thông tin vận đơn
     */
    public Shipment updateShipment(Long shipmentId, String trackingNumber, String carrier,
                                  LocalDateTime estimatedDeliveryAt, String notes) {
        Shipment shipment = shipmentDAO.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Vận đơn không tồn tại"));

        if (trackingNumber != null) shipment.setTrackingNumber(trackingNumber);
        if (carrier != null) shipment.setCarrier(carrier);
        if (estimatedDeliveryAt != null) shipment.setEstimatedDeliveryAt(estimatedDeliveryAt);
        if (notes != null) shipment.setNotes(notes);

        String currentUser = SessionManager.getInstance().getCurrentUsername();
        shipment.setUpdatedBy(currentUser);

        Shipment updated = shipmentDAO.update(shipment);
        logger.info("Cập nhật thông tin vận đơn {}", shipment.getTrackingNumber());
        return updated;
    }

    /**
     * Lấy vận đơn theo ID
     */
    public Optional<Shipment> getShipmentById(Long id) {
        return shipmentDAO.findById(id);
    }

    /**
     * Lấy vận đơn theo mã tracking
     */
    public Optional<Shipment> getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentDAO.findByTrackingNumber(trackingNumber);
    }

    /**
     * Lấy tất cả vận đơn của đơn hàng
     */
    public List<Shipment> getShipmentsByOrder(Long orderId) {
        return shipmentDAO.findByOrderId(orderId);
    }

    /**
     * Lấy vận đơn theo trạng thái
     */
    public List<Shipment> getShipmentsByStatus(ShipmentStatus status) {
        return shipmentDAO.findByStatus(status);
    }

    /**
     * Lấy tất cả vận đơn
     */
    public List<Shipment> getAllShipments() {
        return shipmentDAO.findAll();
    }

    /**
     * Lấy vận đơn quá hạn giao (dự kiến)
     */
    public List<Shipment> getOverdueShipments() {
        return shipmentDAO.findOverdueShipments();
    }

    public void deleteShipment(Long id) {

    }
}

