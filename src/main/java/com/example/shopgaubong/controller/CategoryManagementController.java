package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Category;
import com.example.shopgaubong.service.CategoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

public class CategoryManagementController {

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Long> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colDescription;
    @FXML private TableColumn<Category, Boolean> colActive;

    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<Category> cmbParent;
    @FXML private CheckBox chkActive;
    @FXML private TextField txtSearch;

    private final CategoryService categoryService = new CategoryService();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private Category selectedCategory = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCategories();
        setupTableSelection();
        setupSearchFilter();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("isActive"));

        // Format active column with colored labels
        colActive.setCellFactory(col -> new TableCell<Category, Boolean>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(active ? "✓ Hoạt động" : "✗ Ngừng");
                    setStyle(active ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
    }

    private void loadCategories() {
        List<Category> categories = categoryService.getAllCategories();
        categoryList.setAll(categories);
        categoryTable.setItems(categoryList);

        // Update parent category combo
        cmbParent.setItems(FXCollections.observableArrayList(categories));
        cmbParent.setCellFactory(lv -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        cmbParent.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    private void setupTableSelection() {
        categoryTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showCategoryDetails(newSelection);
                }
            });
    }

    private void setupSearchFilter() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                loadCategories();
            } else {
                filterCategories(newValue.toLowerCase());
            }
        });
    }

    private void filterCategories(String keyword) {
        List<Category> filtered = categoryService.getAllCategories().stream()
            .filter(cat -> cat.getName().toLowerCase().contains(keyword) ||
                         (cat.getDescription() != null &&
                          cat.getDescription().toLowerCase().contains(keyword)))
            .toList();
        categoryList.setAll(filtered);
    }

    private void showCategoryDetails(Category category) {
        selectedCategory = category;
        txtName.setText(category.getName());
        txtDescription.setText(category.getDescription());
        cmbParent.setValue(category.getParent());
        chkActive.setSelected(category.getIsActive());
    }

    @FXML
    private void handleNew() {
        clearForm();
        selectedCategory = null;
        txtName.requestFocus();
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            String name = txtName.getText().trim();
            String description = txtDescription.getText().trim();
            Category parent = cmbParent.getValue();
            Boolean isActive = chkActive.isSelected();

            if (selectedCategory == null) {
                // Create new category
                Long parentId = parent != null ? parent.getId() : null;
                categoryService.createCategory(name, description, parentId);
                showSuccess("Thêm danh mục thành công!");
            } else {
                // Update existing category
                Long parentId = parent != null ? parent.getId() : null;
                categoryService.updateCategory(selectedCategory.getId(), name,
                    description, parentId, isActive);
                showSuccess("Cập nhật danh mục thành công!");
            }

            loadCategories();
            clearForm();
            selectedCategory = null;

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Lỗi khi lưu danh mục: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn danh mục cần xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa danh mục: " + selected.getName());
        confirm.setContentText("Bạn có chắc chắn muốn xóa danh mục này?\n" +
            "Lưu ý: Các danh mục con cũng sẽ bị ảnh hưởng.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                categoryService.deleteCategory(selected.getId());
                showSuccess("Xóa danh mục thành công!");
                loadCategories();
                clearForm();
                selectedCategory = null;
            } catch (Exception e) {
                showError("Lỗi khi xóa danh mục: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
        selectedCategory = null;
        categoryTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleRefresh() {
        loadCategories();
        clearForm();
        selectedCategory = null;
        txtSearch.clear();
    }

    private boolean validateInput() {
        if (txtName.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập tên danh mục!");
            txtName.requestFocus();
            return false;
        }
        return true;
    }

    private void clearForm() {
        txtName.clear();
        txtDescription.clear();
        cmbParent.setValue(null);
        chkActive.setSelected(true);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

