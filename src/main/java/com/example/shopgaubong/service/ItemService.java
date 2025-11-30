package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.CategoryDAO;
import com.example.shopgaubong.dao.ItemDAO;
import com.example.shopgaubong.entity.Category;
import com.example.shopgaubong.entity.Item;
import com.example.shopgaubong.util.SessionManager;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    private final ItemDAO itemDAO;
    private final CategoryDAO categoryDAO;

    public ItemService() {
        this.itemDAO = new ItemDAO();
        this.categoryDAO = new CategoryDAO();
    }

    /**
     * Tạo sản phẩm mới
     */
    public Item createItem(String sku, String name, String description, BigDecimal price,
                          Long categoryId, String unit, BigDecimal weight, String imageUrl) {
        // Validate SKU uniqueness
        Optional<Item> existing = itemDAO.findBySku(sku);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Mã SKU đã tồn tại: " + sku);
        }

        // Get category
        Category category = categoryDAO.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));

        Item item = new Item();
        item.setSku(sku);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setUnit(unit != null ? unit : "Cái");
        item.setWeight(weight);
        item.setImageUrl(imageUrl);
        item.setIsActive(true);

        String currentUser = SessionManager.getInstance().getCurrentUsername();
        item.setCreatedBy(currentUser);
        item.setUpdatedBy(currentUser);

        Item saved = itemDAO.save(item);
        logger.info("Tạo sản phẩm mới: {} - {}", sku, name);
        return saved;
    }

    /**
     * Cập nhật sản phẩm
     */
    public Item updateItem(Long id, String sku, String name, String description, BigDecimal price,
                          Long categoryId, String unit, BigDecimal weight, String imageUrl, Boolean isActive) {
        Item item = itemDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        // Check SKU uniqueness if changed
        if (!item.getSku().equals(sku)) {
            Optional<Item> existing = itemDAO.findBySku(sku);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new IllegalArgumentException("Mã SKU đã tồn tại: " + sku);
            }
        }

        // Get category if changed
        if (!item.getCategory().getId().equals(categoryId)) {
            Category category = categoryDAO.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
            item.setCategory(category);
        }

        item.setSku(sku);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setUnit(unit);
        item.setWeight(weight);
        item.setImageUrl(imageUrl);
        item.setIsActive(isActive);

        String currentUser = SessionManager.getInstance().getCurrentUsername();
        item.setUpdatedBy(currentUser);

        Item updated = itemDAO.update(item);
        logger.info("Cập nhật sản phẩm: {} - {}", sku, name);
        return updated;
    }

    /**
     * Xóa sản phẩm (soft delete)
     */
    public void deleteItem(Long id) {
        Item item = itemDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        item.setIsActive(false);
        String currentUser = SessionManager.getInstance().getCurrentUsername();
        item.setUpdatedBy(currentUser);

        itemDAO.update(item);
        logger.info("Xóa sản phẩm: {} - {}", item.getSku(), item.getName());
    }

    /**
     * Lấy sản phẩm theo ID
     */
    public Optional<Item> getItemById(Long id) {
        return itemDAO.findById(id);
    }

    /**
     * Lấy sản phẩm theo SKU
     */
    public Optional<Item> getItemBySku(String sku) {
        return itemDAO.findBySku(sku);
    }

    /**
     * Lấy tất cả sản phẩm
     */
    public List<Item> getAllItems() {
        return itemDAO.findAll();
    }

    /**
     * Lấy sản phẩm đang hoạt động
     */
    public List<Item> getActiveItems() {
        return itemDAO.findByActive(true);
    }

    /**
     * Lấy sản phẩm theo danh mục
     */
    public List<Item> getItemsByCategory(Long categoryId) {
        return itemDAO.findByCategory(categoryId);
    }

    /**
     * Tìm kiếm sản phẩm theo tên hoặc SKU
     */
    public List<Item> searchItems(String keyword) {
        return itemDAO.search(keyword);
    }
}

