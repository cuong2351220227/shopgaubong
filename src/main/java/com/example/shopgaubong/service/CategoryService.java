package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.CategoryDAO;
import com.example.shopgaubong.entity.Category;
import com.example.shopgaubong.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    /**
     * Tạo danh mục mới
     */
    public Category createCategory(String name, String description, Long parentId) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIsActive(true);

        if (parentId != null) {
            Optional<Category> parentOpt = categoryDAO.findById(parentId);
            parentOpt.ifPresent(category::setParent);
        }

        String currentUser = SessionManager.getInstance().getCurrentUsername();
        category.setCreatedBy(currentUser);
        category.setUpdatedBy(currentUser);

        Category saved = categoryDAO.save(category);
        logger.info("Tạo danh mục mới: {}", name);
        return saved;
    }

    /**
     * Cập nhật danh mục
     */
    public Category updateCategory(Long id, String name, String description, Long parentId, Boolean isActive) {
        Optional<Category> categoryOpt = categoryDAO.findById(id);
        if (categoryOpt.isEmpty()) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }

        Category category = categoryOpt.get();
        category.setName(name);
        category.setDescription(description);
        category.setIsActive(isActive);

        if (parentId != null && !parentId.equals(id)) {
            Optional<Category> parentOpt = categoryDAO.findById(parentId);
            parentOpt.ifPresent(category::setParent);
        } else {
            category.setParent(null);
        }

        String currentUser = SessionManager.getInstance().getCurrentUsername();
        category.setUpdatedBy(currentUser);

        Category updated = categoryDAO.update(category);
        logger.info("Cập nhật danh mục ID {}: {}", id, name);
        return updated;
    }

    /**
     * Xóa danh mục
     */
    public void deleteCategory(Long id) {
        categoryDAO.deleteById(id);
        logger.info("Xóa danh mục ID: {}", id);
    }

    /**
     * Lấy tất cả danh mục
     */
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    /**
     * Lấy các danh mục đang hoạt động
     */
    public List<Category> getActiveCategories() {
        return categoryDAO.findActiveCategories();
    }

    /**
     * Lấy các danh mục gốc
     */
    public List<Category> getRootCategories() {
        return categoryDAO.findRootCategories();
    }

    /**
     * Lấy các danh mục con
     */
    public List<Category> getChildCategories(Long parentId) {
        return categoryDAO.findByParentId(parentId);
    }

    /**
     * Tìm kiếm danh mục theo tên
     */
    public List<Category> searchCategories(String keyword) {
        return categoryDAO.searchByName(keyword);
    }

    /**
     * Lấy danh mục theo ID
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryDAO.findById(id);
    }
}

