package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Category;
import com.example.shopgaubong.entity.Item;
import com.example.shopgaubong.service.CategoryService;
import com.example.shopgaubong.service.ItemService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;

public class ItemManagementController {

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, Long> colId;
    @FXML private TableColumn<Item, String> colSku;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colCategory;
    @FXML private TableColumn<Item, BigDecimal> colPrice;
    @FXML private TableColumn<Item, String> colUnit;
    @FXML private TableColumn<Item, Boolean> colActive;

    @FXML private TextField txtSku;
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<Category> cmbCategory;
    @FXML private TextField txtUnit;
    @FXML private TextField txtWeight;
    @FXML private TextField txtImageUrl;
    @FXML private CheckBox chkActive;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private TextField txtSearch;

    private final ItemService itemService = new ItemService();
    private final CategoryService categoryService = new CategoryService();
    private final ObservableList<Item> itemList = FXCollections.observableArrayList();
    private Item selectedItem = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCategories();
        loadItems();
        setupTableSelection();
        setupSearchFilter();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory().getName()));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("isActive"));

        // Format price column using NumberFormat
        final NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(true);
        colPrice.setCellFactory(col -> new TableCell<Item, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(nf.format(price.longValue()) + " đ");
                }
            }
        });

        // Format active column with colored labels
        colActive.setCellFactory(col -> new TableCell<Item, Boolean>() {
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
        List<Category> categories = categoryService.getActiveCategories();
        cmbCategory.setItems(FXCollections.observableArrayList(categories));
        cmbCategory.setCellFactory(lv -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        cmbCategory.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    private void loadItems() {
        List<Item> items = itemService.getAllItems();
        itemList.setAll(items);
        itemTable.setItems(itemList);
    }

    private void setupTableSelection() {
        itemTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showItemDetails(newSelection);
                }
            });
    }

    private void setupSearchFilter() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                loadItems();
            } else {
                filterItems(newValue.toLowerCase());
            }
        });
    }

    private void filterItems(String keyword) {
        List<Item> filtered = itemService.getAllItems().stream()
            .filter(item -> 
                item.getSku().toLowerCase().contains(keyword) ||
                item.getName().toLowerCase().contains(keyword) ||
                item.getCategory().getName().toLowerCase().contains(keyword))
            .toList();
        itemList.setAll(filtered);
    }

    private void showItemDetails(Item item) {
        selectedItem = item;
        txtSku.setText(item.getSku());
        txtName.setText(item.getName());
        txtDescription.setText(item.getDescription());
        txtPrice.setText(item.getPrice().toString());
        cmbCategory.setValue(item.getCategory());
        txtUnit.setText(item.getUnit());
        txtWeight.setText(item.getWeight() != null ? item.getWeight().toString() : "");
        txtImageUrl.setText(item.getImageUrl());
        chkActive.setSelected(item.getIsActive());
    }

    @FXML
    private void handleNew() {
        clearForm();
        selectedItem = null;
        txtSku.requestFocus();
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            String sku = txtSku.getText().trim();
            String name = txtName.getText().trim();
            String description = txtDescription.getText().trim();
            BigDecimal price = new BigDecimal(txtPrice.getText().trim());
            Category category = cmbCategory.getValue();
            String unit = txtUnit.getText().trim();
            BigDecimal weight = txtWeight.getText().trim().isEmpty() ? null : 
                new BigDecimal(txtWeight.getText().trim());
            String imageUrl = txtImageUrl.getText().trim();
            Boolean isActive = chkActive.isSelected();

            if (selectedItem == null) {
                // Create new item
                itemService.createItem(sku, name, description, price, category.getId(), 
                    unit, weight, imageUrl);
                showSuccess("Thêm sản phẩm thành công!");
            } else {
                // Update existing item
                itemService.updateItem(selectedItem.getId(), sku, name, description, price, 
                    category.getId(), unit, weight, imageUrl, isActive);
                showSuccess("Cập nhật sản phẩm thành công!");
            }

            loadItems();
            clearForm();
            selectedItem = null;

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Lỗi khi lưu sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn sản phẩm cần xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa sản phẩm: " + selected.getName());
        confirm.setContentText("Bạn có chắc chắn muốn xóa sản phẩm này?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                itemService.deleteItem(selected.getId());
                showSuccess("Xóa sản phẩm thành công!");
                loadItems();
                clearForm();
                selectedItem = null;
            } catch (Exception e) {
                showError("Lỗi khi xóa sản phẩm: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
        selectedItem = null;
        itemTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleRefresh() {
        loadItems();
        clearForm();
        selectedItem = null;
        txtSearch.clear();
    }

    private boolean validateInput() {
        if (txtSku.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập mã SKU!");
            txtSku.requestFocus();
            return false;
        }
        if (txtName.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập tên sản phẩm!");
            txtName.requestFocus();
            return false;
        }
        if (txtPrice.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập giá!");
            txtPrice.requestFocus();
            return false;
        }
        try {
            BigDecimal price = new BigDecimal(txtPrice.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                showWarning("Giá phải lớn hơn 0!");
                txtPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showWarning("Giá không hợp lệ!");
            txtPrice.requestFocus();
            return false;
        }
        if (cmbCategory.getValue() == null) {
            showWarning("Vui lòng chọn danh mục!");
            cmbCategory.requestFocus();
            return false;
        }
        if (!txtWeight.getText().trim().isEmpty()) {
            try {
                new BigDecimal(txtWeight.getText().trim());
            } catch (NumberFormatException e) {
                showWarning("Khối lượng không hợp lệ!");
                txtWeight.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void clearForm() {
        txtSku.clear();
        txtName.clear();
        txtDescription.clear();
        txtPrice.clear();
        cmbCategory.setValue(null);
        txtUnit.setText("Cái");
        txtWeight.clear();
        txtImageUrl.clear();
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

