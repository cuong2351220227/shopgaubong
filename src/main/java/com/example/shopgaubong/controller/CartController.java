package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.entity.OrderItem;
import com.example.shopgaubong.service.AuthService;
import com.example.shopgaubong.service.CartService;
import com.example.shopgaubong.service.WarehouseService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @FXML private TableView<OrderItem> cartTable;
    @FXML private TableColumn<OrderItem, String> colItemName;
    @FXML private TableColumn<OrderItem, String> colSKU;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, String> colUnitPrice;
    @FXML private TableColumn<OrderItem, String> colLineTotal;

    @FXML private Label lblSubtotal;
    @FXML private Label lblTax;
    @FXML private Label lblDiscount;
    @FXML private Label lblShippingFee;
    @FXML private Label lblGrandTotal;
    @FXML private Label lblItemCount;

    private final CartService cartService = new CartService();
    private final AuthService authService = new AuthService();
    private final WarehouseService warehouseService = new WarehouseService();

    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();
    private Order currentCart;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCart();
    }

    private void setupTableColumns() {
        colItemName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getItem().getName()));
        
        colSKU.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getItem().getSku()));
        
        colQuantity.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        
        colUnitPrice.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatCurrency(cellData.getValue().getUnitPrice())));
        
        colLineTotal.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatCurrency(cellData.getValue().getLineTotal())));

        // Make quantity column editable
        colQuantity.setCellFactory(col -> new TableCell<OrderItem, Integer>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 999, 1);

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    spinner.getValueFactory().setValue(item);
                    spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (newValue != null && !newValue.equals(oldValue)) {
                            OrderItem orderItem = getTableView().getItems().get(getIndex());
                            updateQuantity(orderItem, newValue);
                        }
                    });
                    setGraphic(spinner);
                }
            }
        });

        // Add action column for delete button
        TableColumn<OrderItem, Void> colAction = new TableColumn<>("Hành động");
        colAction.setCellFactory(col -> new TableCell<OrderItem, Void>() {
            private final Button btnDelete = new Button("🗑️ Xóa");

            {
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                btnDelete.setOnAction(event -> {
                    OrderItem orderItem = getTableView().getItems().get(getIndex());
                    removeItem(orderItem);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDelete);
                }
            }
        });
        cartTable.getColumns().add(colAction);

        cartTable.setItems(cartItems);
    }

    private void loadCart() {
        try {
            Long customerId = authService.getCurrentAccount().getId();
            currentCart = cartService.getCurrentCart(customerId);
            
            cartItems.clear();
            cartItems.addAll(currentCart.getOrderItems());
            
            updateSummary();
            
            logger.info("Tải giỏ hàng thành công: {} sản phẩm", cartItems.size());
        } catch (Exception e) {
            logger.error("Lỗi khi tải giỏ hàng: {}", e.getMessage(), e);
            showError("Không thể tải giỏ hàng: " + e.getMessage());
        }
    }

    private void updateQuantity(OrderItem orderItem, Integer newQuantity) {
        try {
            Long customerId = authService.getCurrentAccount().getId();
            // Find warehouse that has this item
            Long warehouseId = findWarehouseForItem(orderItem.getItem().getId());
            
            cartService.updateCartItem(customerId, orderItem.getId(), newQuantity, warehouseId);
            
            loadCart(); // Reload to update totals
            showSuccess("Cập nhật số lượng thành công!");
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật số lượng: {}", e.getMessage(), e);
            showError("Không thể cập nhật số lượng: " + e.getMessage());
            loadCart(); // Reload to revert changes
        }
    }

    private void removeItem(OrderItem orderItem) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa sản phẩm khỏi giỏ hàng");
        confirm.setContentText("Bạn có chắc chắn muốn xóa " + orderItem.getItem().getName() + " khỏi giỏ hàng?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Long customerId = authService.getCurrentAccount().getId();
                cartService.removeFromCart(customerId, orderItem.getId());
                
                loadCart();
                showSuccess("Đã xóa sản phẩm khỏi giỏ hàng!");
            } catch (Exception e) {
                logger.error("Lỗi khi xóa sản phẩm: {}", e.getMessage(), e);
                showError("Không thể xóa sản phẩm: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearCart() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa toàn bộ giỏ hàng");
        confirm.setContentText("Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Long customerId = authService.getCurrentAccount().getId();
                cartService.clearCart(customerId);
                
                loadCart();
                showSuccess("Đã xóa toàn bộ giỏ hàng!");
            } catch (Exception e) {
                logger.error("Lỗi khi xóa giỏ hàng: {}", e.getMessage(), e);
                showError("Không thể xóa giỏ hàng: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showWarning("Giỏ hàng trống! Vui lòng thêm sản phẩm trước khi thanh toán.");
            return;
        }

        try {
            // Load checkout view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/checkout-view.fxml"));
            VBox checkoutView = loader.load();
            
            // Pass current cart to checkout controller
            CheckoutController checkoutController = loader.getController();
            checkoutController.setCart(currentCart);
            
            // Replace current view with checkout view
            Pane parentView = (Pane) cartTable.getParent().getParent();
            parentView.getChildren().clear();
            parentView.getChildren().add(checkoutView);
            
        } catch (Exception e) {
            logger.error("Lỗi khi chuyển sang checkout: {}", e.getMessage(), e);
            showError("Không thể chuyển sang trang thanh toán: " + e.getMessage());
        }
    }

    @FXML
    private void handleContinueShopping() {
        // Return to product catalog
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/product-catalog-view.fxml"));
            VBox catalogView = loader.load();
            
            VBox parentView = (VBox) cartTable.getParent().getParent();
            parentView.getChildren().clear();
            parentView.getChildren().add(catalogView);
            
        } catch (Exception e) {
            logger.error("Lỗi khi quay lại catalog: {}", e.getMessage(), e);
            showError("Không thể quay lại trang sản phẩm: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadCart();
        showSuccess("Đã làm mới giỏ hàng!");
    }

    private void updateSummary() {
        if (currentCart != null) {
            lblSubtotal.setText(formatCurrency(currentCart.getSubtotal()));
            lblTax.setText(formatCurrency(currentCart.getTax()));
            lblDiscount.setText(formatCurrency(currentCart.getDiscount()));
            lblShippingFee.setText(formatCurrency(currentCart.getShippingFee()));
            lblGrandTotal.setText(formatCurrency(currentCart.getGrandTotal()));
            lblItemCount.setText(String.valueOf(cartItems.size()) + " sản phẩm");
        } else {
            lblSubtotal.setText(formatCurrency(BigDecimal.ZERO));
            lblTax.setText(formatCurrency(BigDecimal.ZERO));
            lblDiscount.setText(formatCurrency(BigDecimal.ZERO));
            lblShippingFee.setText(formatCurrency(BigDecimal.ZERO));
            lblGrandTotal.setText(formatCurrency(BigDecimal.ZERO));
            lblItemCount.setText("0 sản phẩm");
        }
    }

    private Long getDefaultWarehouseId() {
        // Get first active warehouse
        return warehouseService.getActiveWarehouses().stream()
            .findFirst()
            .map(w -> w.getId())
            .orElseThrow(() -> new IllegalStateException("Không tìm thấy kho hoạt động"));
    }
    
    private Long findWarehouseForItem(Long itemId) {
        // Find warehouse that has this item with available stock
        try {
            var stockService = new com.example.shopgaubong.service.StockService();
            var stockItems = stockService.getStockItemsByItem(itemId);
            
            // Find warehouse with available stock
            for (var stockItem : stockItems) {
                if (stockItem.getAvailable() > 0) {
                    return stockItem.getWarehouse().getId();
                }
            }
            
            // If no warehouse has available stock, return first warehouse with this item
            if (!stockItems.isEmpty()) {
                return stockItems.get(0).getWarehouse().getId();
            }
            
            // Fallback to default warehouse
            return getDefaultWarehouseId();
        } catch (Exception e) {
            logger.warn("Cannot find warehouse for item {}, using default", itemId);
            return getDefaultWarehouseId();
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        return currencyFormat.format(amount);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Platform.runLater(alert::showAndWait);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Platform.runLater(alert::showAndWait);
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Platform.runLater(alert::showAndWait);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Platform.runLater(alert::showAndWait);
    }
}
