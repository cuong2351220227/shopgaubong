package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.entity.OrderItem;
import com.example.shopgaubong.service.AuthService;
import com.example.shopgaubong.service.CartService;
import com.example.shopgaubong.service.WarehouseService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    @FXML private TableView<OrderItem> orderItemsTable;
    @FXML private TableColumn<OrderItem, String> colItemName;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, String> colUnitPrice;
    @FXML private TableColumn<OrderItem, String> colLineTotal;

    @FXML private Label lblCartItemCount;
    @FXML private Label lblCartSummary;

    @FXML private TextField txtReceiverName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtAddress;
    @FXML private TextField txtCity;
    @FXML private TextField txtDistrict;
    @FXML private TextField txtWard;
    @FXML private TextArea txtNote;

    @FXML private Label lblSubtotal;
    @FXML private Label lblTax;
    @FXML private Label lblDiscount;
    @FXML private Label lblShippingFee;
    @FXML private Label lblGrandTotal;
    
    @FXML private TextField txtPromotionCode;
    @FXML private Button btnApplyPromotion;
    @FXML private Button btnRemovePromotion;
    @FXML private Label lblPromotionInfo;

    private final CartService cartService = new CartService();
    private final AuthService authService = new AuthService();
    private final WarehouseService warehouseService = new WarehouseService();
    private final com.example.shopgaubong.service.PromotionService promotionService = new com.example.shopgaubong.service.PromotionService();

    private Order cart;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCustomerInfo();
    }

    public void setCart(Order cart) {
        this.cart = cart;
        loadCartItems();
        updateSummary();
    }

    private void setupTableColumns() {
        colItemName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getItem().getName()));
        
        colQuantity.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        
        colUnitPrice.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatCurrency(cellData.getValue().getUnitPrice())));
        
        colLineTotal.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatCurrency(cellData.getValue().getLineTotal())));

        orderItemsTable.setItems(orderItems);
    }

    private void loadCustomerInfo() {
        try {
            var profile = authService.getCurrentAccount().getProfile();
            txtReceiverName.setText(profile.getFullName());
            txtPhone.setText(profile.getPhoneNumber());
            if (profile.getAddress() != null) {
                txtAddress.setText(profile.getAddress());
            }
        } catch (Exception e) {
            logger.error("Lỗi khi tải thông tin khách hàng: {}", e.getMessage(), e);
        }
    }

    private void loadCartItems() {
        if (cart != null) {
            orderItems.clear();
            orderItems.addAll(cart.getOrderItems());
            updateCartInfo();
        }
    }

    private void updateCartInfo() {
        if (cart != null && !cart.getOrderItems().isEmpty()) {
            int totalItems = cart.getOrderItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
            int uniqueItems = cart.getOrderItems().size();
            
            lblCartItemCount.setText(uniqueItems + " sản phẩm (" + totalItems + " món)");
            lblCartSummary.setText(String.format("Tổng %d loại sản phẩm, %d món", uniqueItems, totalItems));
        } else {
            lblCartItemCount.setText("0 sản phẩm");
            lblCartSummary.setText("Giỏ hàng trống");
        }
    }

    private void updateSummary() {
        if (cart != null) {
            lblSubtotal.setText(formatCurrency(cart.getSubtotal()));
            lblTax.setText(formatCurrency(cart.getTax()));
            lblDiscount.setText(formatCurrency(cart.getDiscount()));
            lblShippingFee.setText(formatCurrency(cart.getShippingFee()));
            lblGrandTotal.setText(formatCurrency(cart.getGrandTotal()));
        }
    }

    @FXML
    private void handleApplyPromotion() {
        String code = txtPromotionCode.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            showWarning("Vui lòng nhập mã khuyến mãi");
            return;
        }
        
        try {
            Long customerId = authService.getCurrentAccount().getId();
            cartService.applyPromotionCode(customerId, code);
            
            // Reload cart to get updated discount
            cart = cartService.getCurrentCart(customerId);
            updateSummary();
            
            // Show promotion info
            if (cart.getPromotion() != null) {
                var promo = cart.getPromotion();
                String info = String.format("✓ Đã áp dụng: %s - %s", 
                    promo.getCode(), promo.getName());
                lblPromotionInfo.setText(info);
                lblPromotionInfo.setVisible(true);
                btnRemovePromotion.setVisible(true);
                txtPromotionCode.setDisable(true);
                btnApplyPromotion.setDisable(true);
                
                showSuccess("Áp dụng mã khuyến mãi thành công!");
            }
        } catch (Exception e) {
            logger.error("Error applying promotion: {}", e.getMessage(), e);
            showError("Không thể áp dụng mã khuyến mãi: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRemovePromotion() {
        try {
            Long customerId = authService.getCurrentAccount().getId();
            cartService.removePromotionCode(customerId);
            
            // Reload cart to get updated values
            cart = cartService.getCurrentCart(customerId);
            updateSummary();
            
            // Update UI
            lblPromotionInfo.setVisible(false);
            btnRemovePromotion.setVisible(false);
            txtPromotionCode.setDisable(false);
            txtPromotionCode.clear();
            btnApplyPromotion.setDisable(false);
            
            showSuccess("Đã hủy mã khuyến mãi");
        } catch (Exception e) {
            logger.error("Error removing promotion: {}", e.getMessage(), e);
            showError("Không thể hủy mã khuyến mãi: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewPromotions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/promotion-list-view.fxml"));
            Parent promotionsView = loader.load();
            
            // Open in a new window/dialog instead of replacing content
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(orderItemsTable.getScene().getWindow());
            dialogStage.setTitle("Danh sách khuyến mãi");
            
            Scene scene = new Scene(promotionsView);
            dialogStage.setScene(scene);
            dialogStage.setWidth(800);
            dialogStage.setHeight(600);
            dialogStage.showAndWait();
        } catch (Exception e) {
            logger.error("Error viewing promotions: {}", e.getMessage(), e);
            showError("Không thể xem danh sách khuyến mãi: " + e.getMessage());
        }
    }

    @FXML
    private void handlePlaceOrder() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận đặt hàng");
        confirm.setHeaderText("Xác nhận đặt hàng");
        confirm.setContentText("Bạn có chắc chắn muốn đặt hàng với tổng tiền " + 
                              formatCurrency(cart.getGrandTotal()) + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                placeOrder();
            }
        });
    }

    private boolean validateInputs() {
        if (txtReceiverName.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập tên người nhận!");
            txtReceiverName.requestFocus();
            return false;
        }

        if (txtPhone.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập số điện thoại!");
            txtPhone.requestFocus();
            return false;
        }

        if (txtAddress.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập địa chỉ giao hàng!");
            txtAddress.requestFocus();
            return false;
        }

        if (txtCity.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập thành phố!");
            txtCity.requestFocus();
            return false;
        }

        if (txtDistrict.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập quận/huyện!");
            txtDistrict.requestFocus();
            return false;
        }

        if (txtWard.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập phường/xã!");
            txtWard.requestFocus();
            return false;
        }

        // Validate phone number format
        String phone = txtPhone.getText().trim();
        if (!phone.matches("^[0-9]{10,11}$")) {
            showWarning("Số điện thoại không hợp lệ! Vui lòng nhập 10-11 chữ số.");
            txtPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void placeOrder() {
        try {
            Long customerId = authService.getCurrentAccount().getId();
            Long warehouseId = getDefaultWarehouseId();
            
            String receiverName = txtReceiverName.getText().trim();
            String phone = txtPhone.getText().trim();
            String address = txtAddress.getText().trim();
            String city = txtCity.getText().trim();
            String district = txtDistrict.getText().trim();
            String ward = txtWard.getText().trim();

            Order placedOrder = cartService.checkout(
                customerId, 
                address, 
                city, 
                district, 
                ward, 
                phone, 
                receiverName, 
                warehouseId
            );

            logger.info("Đặt hàng thành công: {}", placedOrder.getOrderNumber());
            
            showSuccess("Đặt hàng thành công!\n" +
                       "Mã đơn hàng: " + placedOrder.getOrderNumber() + "\n" +
                       "Vui lòng thanh toán để hoàn tất đơn hàng.");
            
            // Navigate to payment view or order list
            navigateToPayment(placedOrder);
            
        } catch (Exception e) {
            logger.error("Lỗi khi đặt hàng: {}", e.getMessage(), e);
            showError("Không thể đặt hàng: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/cart-view.fxml"));
            VBox cartView = loader.load();
            
            VBox parentView = (VBox) orderItemsTable.getParent().getParent().getParent();
            parentView.getChildren().clear();
            parentView.getChildren().add(cartView);
            
        } catch (Exception e) {
            logger.error("Lỗi khi quay lại giỏ hàng: {}", e.getMessage(), e);
            showError("Không thể quay lại giỏ hàng: " + e.getMessage());
        }
    }

    private void navigateToPayment(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/payment-view.fxml"));
            VBox paymentView = loader.load();
            
            // Pass the order to payment controller
            PaymentController paymentController = loader.getController();
            paymentController.setOrder(order);
            
            VBox parentView = (VBox) orderItemsTable.getParent().getParent().getParent();
            parentView.getChildren().clear();
            parentView.getChildren().add(paymentView);
            
        } catch (Exception e) {
            logger.error("Lỗi khi chuyển sang thanh toán: {}", e.getMessage(), e);
            showError("Không thể chuyển sang trang thanh toán: " + e.getMessage());
        }
    }

    private Long getDefaultWarehouseId() {
        return warehouseService.getActiveWarehouses().stream()
            .findFirst()
            .map(w -> w.getId())
            .orElseThrow(() -> new IllegalStateException("Không tìm thấy kho hoạt động"));
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
