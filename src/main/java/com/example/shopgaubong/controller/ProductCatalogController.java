package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Category;
import com.example.shopgaubong.entity.Item;
import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.entity.OrderItem;
import com.example.shopgaubong.service.AuthService;
import com.example.shopgaubong.service.CategoryService;
import com.example.shopgaubong.service.ItemService;
import com.example.shopgaubong.service.OrderService;
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

public class ProductCatalogController {

    @FXML private ComboBox<Category> cmbCategory;
    @FXML private TextField txtSearch;
    @FXML private TableView<Item> productTable;
    @FXML private TableColumn<Item, String> colSku;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colCategory;
    @FXML private TableColumn<Item, BigDecimal> colPrice;
    @FXML private TableColumn<Item, String> colUnit;

    @FXML private Label lblProductName;
    @FXML private Label lblProductSku;
    @FXML private Label lblProductPrice;
    @FXML private Label lblProductCategory;
    @FXML private TextArea txtProductDescription;
    @FXML private Spinner<Integer> spnQuantity;
    @FXML private Button btnAddToCart;

    private final ItemService itemService = new ItemService();
    private final CategoryService categoryService = new CategoryService();
    private final OrderService orderService = new OrderService();
    private final AuthService authService = new AuthService();
    private final ObservableList<Item> productList = FXCollections.observableArrayList();
    private Item selectedItem = null;
    private static final Long DEFAULT_WAREHOUSE_ID = 1L; // Default warehouse

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCategories();
        loadProducts();
        setupTableSelection();
        setupSearchFilter();
        setupQuantitySpinner();
        clearProductDetails();
    }

    private void setupTableColumns() {
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCategory().getName()));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));

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
    }

    private void loadCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        ObservableList<Category> categoryObsList = FXCollections.observableArrayList(categories);
        categoryObsList.add(0, null); // Add "All" option

        cmbCategory.setItems(categoryObsList);
        cmbCategory.setCellFactory(lv -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "-- Tất cả danh mục --" : item.getName()));
            }
        });
        cmbCategory.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "-- Tất cả danh mục --" : item.getName()));
            }
        });
        cmbCategory.setValue(null); // Select "All" by default

        cmbCategory.valueProperty().addListener((obs, oldVal, newVal) -> filterProducts());
    }

    private void loadProducts() {
        List<Item> items = itemService.getActiveItems();
        productList.setAll(items);
        productTable.setItems(productList);
    }

    private void setupTableSelection() {
        productTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showProductDetails(newSelection);
                } else {
                    clearProductDetails();
                }
            });
    }

    private void setupSearchFilter() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> filterProducts());
    }

    private void setupQuantitySpinner() {
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spnQuantity.setValueFactory(valueFactory);
    }

    private void filterProducts() {
        String keyword = txtSearch.getText();
        Category category = cmbCategory.getValue();

        List<Item> filtered = itemService.getActiveItems();

        // Filter by category
        if (category != null) {
            filtered = filtered.stream()
                .filter(item -> item.getCategory().getId().equals(category.getId()))
                .toList();
        }

        // Filter by keyword
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            filtered = filtered.stream()
                .filter(item ->
                    item.getSku().toLowerCase().contains(lowerKeyword) ||
                    item.getName().toLowerCase().contains(lowerKeyword) ||
                    item.getCategory().getName().toLowerCase().contains(lowerKeyword))
                .toList();
        }

        productList.setAll(filtered);
    }

    private void showProductDetails(Item item) {
        selectedItem = item;
        lblProductName.setText(item.getName());
        lblProductSku.setText("SKU: " + item.getSku());
        lblProductPrice.setText(String.format("%,.0f đ / %s", item.getPrice(), item.getUnit()));
        lblProductCategory.setText("Danh mục: " + item.getCategory().getName());
        txtProductDescription.setText(item.getDescription() != null ? item.getDescription() : "Không có mô tả");
        btnAddToCart.setDisable(false);
    }

    private void clearProductDetails() {
        selectedItem = null;
        lblProductName.setText("Chưa chọn sản phẩm");
        lblProductSku.setText("");
        lblProductPrice.setText("");
        lblProductCategory.setText("");
        txtProductDescription.setText("");
        btnAddToCart.setDisable(true);
    }

    @FXML
    private void handleAddToCart() {
        if (selectedItem == null) {
            showWarning("Vui lòng chọn sản phẩm!");
            return;
        }

        try {
            int quantity = spnQuantity.getValue();
            Long customerId = authService.getCurrentAccount().getId();

            // Get or create cart (order with CART status)
            Order cart = orderService.getActiveCartForCustomer(customerId);

            // Check if item already in cart
            Optional<OrderItem> existingItem = cart.getOrderItems().stream()
                .filter(oi -> oi.getItem().getId().equals(selectedItem.getId()))
                .findFirst();

            if (existingItem.isPresent()) {
                // Item already in cart, inform user
                showInfo(String.format("Sản phẩm '%s' đã có trong giỏ hàng!\nVui lòng vào 'Giỏ hàng' để cập nhật số lượng.",
                    selectedItem.getName()));
            } else {
                // Add new item
                orderService.addItemToOrder(cart.getId(), selectedItem, quantity, DEFAULT_WAREHOUSE_ID);
                showSuccess(String.format("Đã thêm vào giỏ hàng: %s (x%d)",
                    selectedItem.getName(), quantity));
            }

            // Reset quantity spinner
            spnQuantity.getValueFactory().setValue(1);

        } catch (IllegalStateException e) {
            showError("Không thể thêm vào giỏ hàng: " + e.getMessage());
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadProducts();
        txtSearch.clear();
        cmbCategory.setValue(null);
        clearProductDetails();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
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
