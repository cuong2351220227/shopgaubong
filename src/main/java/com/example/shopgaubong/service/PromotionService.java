package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.PromotionDAO;
import com.example.shopgaubong.entity.Promotion;
import com.example.shopgaubong.enums.PromotionType;
import com.example.shopgaubong.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PromotionService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionService.class);
    private final PromotionDAO promotionDAO;

    public PromotionService() {
        this.promotionDAO = new PromotionDAO();
    }

    /**
     * Tạo khuyến mãi mới
     */
    public Promotion createPromotion(String code, String name, String description,
                                    PromotionType type, BigDecimal value,
                                    BigDecimal minOrderValue, BigDecimal maxDiscountAmount,
                                    LocalDateTime startDate, LocalDateTime endDate,
                                    Integer maxUsage) {
        // Validate code uniqueness
        Optional<Promotion> existing = promotionDAO.findByCode(code);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại: " + code);
        }

        // Validate dates
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        Promotion promotion = new Promotion();
        promotion.setCode(code);
        promotion.setName(name);
        promotion.setDescription(description);
        promotion.setType(type);
        promotion.setValue(value);
        promotion.setMinOrderValue(minOrderValue != null ? minOrderValue : BigDecimal.ZERO);
        promotion.setMaxDiscountAmount(maxDiscountAmount);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setMaxUsage(maxUsage);
        promotion.setCurrentUsage(0);
        promotion.setIsActive(true);

        String currentUser = SessionManager.getInstance().getCurrentUsername();
        promotion.setCreatedBy(currentUser);
        promotion.setUpdatedBy(currentUser);

        Promotion saved = promotionDAO.save(promotion);
        logger.info("Tạo khuyến mãi mới: {} - {}", code, name);
        return saved;
    }

    /**
     * Cập nhật khuyến mãi
     */
    public Promotion updatePromotion(Long id, String code, String name, String description,
                                    PromotionType type, BigDecimal value,
                                    BigDecimal minOrderValue, BigDecimal maxDiscountAmount,
                                    LocalDateTime startDate, LocalDateTime endDate,
                                    Integer maxUsage, Boolean isActive) {
        Promotion promotion = promotionDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khuyến mãi không tồn tại"));

        // Check code uniqueness if changed
        if (!promotion.getCode().equals(code)) {
            Optional<Promotion> existing = promotionDAO.findByCode(code);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại: " + code);
            }
        }

        // Validate dates
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        promotion.setCode(code);
        promotion.setName(name);
        promotion.setDescription(description);
        promotion.setType(type);
        promotion.setValue(value);
        promotion.setMinOrderValue(minOrderValue != null ? minOrderValue : BigDecimal.ZERO);
        promotion.setMaxDiscountAmount(maxDiscountAmount);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setMaxUsage(maxUsage);
        promotion.setIsActive(isActive);

        String currentUser = SessionManager.getInstance().getCurrentUsername();
        promotion.setUpdatedBy(currentUser);

        Promotion updated = promotionDAO.update(promotion);
        logger.info("Cập nhật khuyến mãi: {} - {}", code, name);
        return updated;
    }

    /**
     * Xóa khuyến mãi (soft delete)
     */
    public void deletePromotion(Long id) {
        Promotion promotion = promotionDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khuyến mãi không tồn tại"));

        promotion.setIsActive(false);
        String currentUser = SessionManager.getInstance().getCurrentUsername();
        promotion.setUpdatedBy(currentUser);

        promotionDAO.update(promotion);
        logger.info("Xóa khuyến mãi: {} - {}", promotion.getCode(), promotion.getName());
    }

    /**
     * Áp dụng khuyến mãi cho đơn hàng
     */
    public Promotion applyPromotion(String code, BigDecimal orderAmount) {
        Optional<Promotion> promotionOpt = promotionDAO.findByCode(code);
        if (promotionOpt.isEmpty()) {
            throw new IllegalArgumentException("Mã khuyến mãi không tồn tại");
        }

        Promotion promotion = promotionOpt.get();

        if (!promotion.isValid()) {
            throw new IllegalArgumentException("Mã khuyến mãi không hợp lệ hoặc đã hết hạn");
        }

        if (orderAmount.compareTo(promotion.getMinOrderValue()) < 0) {
            throw new IllegalArgumentException(
                    String.format("Giá trị đơn hàng tối thiểu: %,.0f VNĐ", promotion.getMinOrderValue())
            );
        }

        return promotion;
    }

    /**
     * Tính toán số tiền giảm giá
     */
    public BigDecimal calculateDiscount(Promotion promotion, BigDecimal orderAmount) {
        if (promotion == null || !promotion.isValid()) {
            return BigDecimal.ZERO;
        }
        return promotion.calculateDiscount(orderAmount);
    }

    /**
     * Tăng số lần sử dụng của khuyến mãi
     */
    public void incrementUsage(Long promotionId) {
        Promotion promotion = promotionDAO.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Khuyến mãi không tồn tại"));

        promotion.setCurrentUsage(promotion.getCurrentUsage() + 1);
        promotionDAO.update(promotion);

        logger.info("Tăng số lần sử dụng khuyến mãi: {} (lần {})",
                promotion.getCode(), promotion.getCurrentUsage());
    }

    /**
     * Lấy khuyến mãi theo ID
     */
    public Optional<Promotion> getPromotionById(Long id) {
        return promotionDAO.findById(id);
    }

    /**
     * Lấy khuyến mãi theo mã
     */
    public Optional<Promotion> getPromotionByCode(String code) {
        return promotionDAO.findByCode(code);
    }

    /**
     * Lấy tất cả khuyến mãi
     */
    public List<Promotion> getAllPromotions() {
        return promotionDAO.findAll();
    }

    /**
     * Lấy khuyến mãi đang hoạt động
     */
    public List<Promotion> getActivePromotions() {
        return promotionDAO.findActivePromotions();
    }

    /**
     * Lấy khuyến mãi hợp lệ (trong thời gian và chưa hết lượt)
     */
    public List<Promotion> getValidPromotions() {
        return promotionDAO.findValidPromotions();
    }

    public Promotion createPromotion(String code, String name, PromotionType type, BigDecimal discountValue, BigDecimal minOrderValue, BigDecimal maxDiscount, Integer usageLimit, LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }

    public void updatePromotion(Promotion promotion) {
    }
}

