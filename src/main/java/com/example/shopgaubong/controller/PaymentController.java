package com.example.shopgaubong.controller;

import com.example.shopgaubong.dao.OrderDAO;
import com.example.shopgaubong.dto.FeeCalculation;
import com.example.shopgaubong.dto.PaymentRequest;
import com.example.shopgaubong.dto.PaymentResponse;
import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.entity.OrderItem;
import com.example.shopgaubong.enums.OrderStatus;
import com.example.shopgaubong.enums.PaymentMethod;
import com.example.shopgaubong.service.PaymentService;
import com.example.shopgaubong.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.math.BigDecimal;
import java.net.URI;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    // Order items table
    @FXML private TableView<OrderItem> orderItemsTable;
    @FXML private TableColumn<OrderItem, String> colProductName;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, String> colUnitPrice;
    @FXML private TableColumn<OrderItem, String> colTotal;

    // Order info labels
    @FXML private Label lblOrderNumber;
    @FXML private Label lblOrderStatus;
    @FXML private Label lblReceiverName;
    @FXML private Label lblReceiverPhone;
    @FXML private Label lblAddress;

    // Payment section
    @FXML private ComboBox<PaymentMethod> paymentMethodCombo;
    @FXML private Label subtotalLabel;
    @FXML private Label codFeeLabel;
    @FXML private Label gatewayFeeLabel;
    @FXML private Label shippingFeeLabel;
    @FXML private Label totalLabel;
    @FXML private Button payButton;
    
    // Payment details boxes
    @FXML private javafx.scene.layout.VBox paymentDetailsBox;
    @FXML private javafx.scene.layout.VBox codDetailsBox;
    @FXML private javafx.scene.layout.VBox bankTransferDetailsBox;
    @FXML private javafx.scene.layout.VBox vnpayDetailsBox;
    @FXML private javafx.scene.layout.VBox momoDetailsBox;
    @FXML private javafx.scene.layout.VBox sepayDetailsBox;
    @FXML private Label bankTransferContent;

    private final PaymentService paymentService = new PaymentService();
    private final OrderDAO orderDAO = new OrderDAO();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    private Order currentOrder;

    @FXML
    public void initialize() {
        setupOrderItemsTable();
        setupPaymentMethodCombo();
        
        // Disable payment button initially
        payButton.setDisable(true);

        // Update fees and details when payment method changes
        paymentMethodCombo.setOnAction(e -> {
            updateFeeCalculation();
            updatePaymentMethodDetails();
        });
    }

    /**
     * Set order from checkout - this is the main entry point
     */
    public void setOrder(Order order) {
        this.currentOrder = order;
        displayOrderInfo();
        displayOrderItems();
        
        // Auto-select default payment method
        if (paymentMethodCombo.getValue() == null) {
            paymentMethodCombo.setValue(PaymentMethod.COD);
        }
        
        updateFeeCalculation();
        payButton.setDisable(false);
        
        logger.info("Loaded order {} for payment", order.getOrderNumber());
    }

    private void setupOrderItemsTable() {
        colProductName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getItem().getName()));
        
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        colUnitPrice.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatCurrency(cellData.getValue().getUnitPrice())));
        
        colTotal.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatCurrency(cellData.getValue().getLineTotal())));

        // Style for quantity column
        colQuantity.setCellFactory(col -> new TableCell<OrderItem, Integer>() {
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                } else {
                    setText("× " + quantity);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");
                }
            }
        });

        orderItemsTable.setItems(orderItems);
    }

    private void setupPaymentMethodCombo() {
        paymentMethodCombo.setItems(FXCollections.observableArrayList(
            PaymentMethod.COD,
            PaymentMethod.BANK_TRANSFER,
            PaymentMethod.VNPAY,
            PaymentMethod.MOMO,
            PaymentMethod.SEPAY
        ));

        // Custom display for combo box
        paymentMethodCombo.setCellFactory(lv -> new ListCell<PaymentMethod>() {
            @Override
            protected void updateItem(PaymentMethod item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });

        paymentMethodCombo.setButtonCell(new ListCell<PaymentMethod>() {
            @Override
            protected void updateItem(PaymentMethod item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
    }

    private void updatePaymentMethodDetails() {
        PaymentMethod selected = paymentMethodCombo.getValue();
        
        if (selected == null) {
            paymentDetailsBox.setVisible(false);
            paymentDetailsBox.setManaged(false);
            return;
        }
        
        // Show main details box
        paymentDetailsBox.setVisible(true);
        paymentDetailsBox.setManaged(true);
        
        // Hide all detail boxes first
        codDetailsBox.setVisible(false);
        codDetailsBox.setManaged(false);
        bankTransferDetailsBox.setVisible(false);
        bankTransferDetailsBox.setManaged(false);
        vnpayDetailsBox.setVisible(false);
        vnpayDetailsBox.setManaged(false);
        momoDetailsBox.setVisible(false);
        momoDetailsBox.setManaged(false);
        sepayDetailsBox.setVisible(false);
        sepayDetailsBox.setManaged(false);
        
        // Show selected method's detail box
        switch (selected) {
            case COD -> {
                codDetailsBox.setVisible(true);
                codDetailsBox.setManaged(true);
            }
            case BANK_TRANSFER -> {
                bankTransferDetailsBox.setVisible(true);
                bankTransferDetailsBox.setManaged(true);
                // Update bank transfer content with order number
                if (currentOrder != null) {
                    bankTransferContent.setText("TT " + currentOrder.getOrderNumber());
                }
            }
            case VNPAY -> {
                vnpayDetailsBox.setVisible(true);
                vnpayDetailsBox.setManaged(true);
            }
            case MOMO -> {
                momoDetailsBox.setVisible(true);
                momoDetailsBox.setManaged(true);
            }
            case SEPAY -> {
                sepayDetailsBox.setVisible(true);
                sepayDetailsBox.setManaged(true);
            }
        }
        
        logger.debug("Updated payment method details for: {}", selected);
    }

    private void displayOrderInfo() {
        if (currentOrder == null) return;
        
        lblOrderNumber.setText(currentOrder.getOrderNumber());
        lblOrderStatus.setText(getStatusDisplayName(currentOrder.getStatus()));
        lblReceiverName.setText(currentOrder.getShippingReceiverName() != null ? currentOrder.getShippingReceiverName() : "-");
        lblReceiverPhone.setText(currentOrder.getShippingPhone() != null ? currentOrder.getShippingPhone() : "-");
        
        // Build full address
        StringBuilder address = new StringBuilder();
        if (currentOrder.getShippingAddress() != null) {
            address.append(currentOrder.getShippingAddress());
        }
        if (currentOrder.getShippingWard() != null) {
            if (address.length() > 0) address.append(", ");
            address.append(currentOrder.getShippingWard());
        }
        if (currentOrder.getShippingDistrict() != null) {
            if (address.length() > 0) address.append(", ");
            address.append(currentOrder.getShippingDistrict());
        }
        if (currentOrder.getShippingCity() != null) {
            if (address.length() > 0) address.append(", ");
            address.append(currentOrder.getShippingCity());
        }
        lblAddress.setText(address.length() > 0 ? address.toString() : "-");
    }

    private void displayOrderItems() {
        if (currentOrder == null || currentOrder.getOrderItems() == null) return;
        
        orderItems.clear();
        orderItems.addAll(currentOrder.getOrderItems());
        
        logger.info("Displaying {} items for order {}", orderItems.size(), currentOrder.getOrderNumber());
    }

    private void updateFeeCalculation() {
        if (currentOrder == null || paymentMethodCombo.getValue() == null) return;

        try {
            PaymentMethod method = paymentMethodCombo.getValue();
            FeeCalculation fee = paymentService.calculateFees(currentOrder, method);

            subtotalLabel.setText(formatCurrency(fee.getSubtotal()));
            codFeeLabel.setText(formatCurrency(fee.getCodFee()));
            gatewayFeeLabel.setText(formatCurrency(fee.getGatewayFee()));
            shippingFeeLabel.setText(formatCurrency(fee.getShippingFee()));
            totalLabel.setText(formatCurrency(fee.getGrandTotal()));

        } catch (Exception e) {
            logger.error("Error calculating fees: {}", e.getMessage(), e);
            showError("Lỗi tính phí: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayment() {
        if (currentOrder == null) {
            showError("Không có đơn hàng để thanh toán");
            return;
        }

        if (paymentMethodCombo.getValue() == null) {
            showError("Vui lòng chọn phương thức thanh toán");
            return;
        }

        PaymentMethod selectedMethod = paymentMethodCombo.getValue();

        // Confirm payment with method-specific message
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Bạn có chắc muốn thanh toán đơn hàng này?");
        
        String confirmMessage = buildConfirmMessage(selectedMethod);
        confirm.setContentText(confirmMessage);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(currentOrder.getId());
            request.setPaymentMethod(selectedMethod);
            request.setAmount(currentOrder.getGrandTotal());
            request.setReturnUrl("http://localhost:8080/payment/return");
            request.setIpAddress("127.0.0.1");

            PaymentResponse response = paymentService.createPayment(request);

            if (response.isSuccess()) {
                handleSuccessfulPayment(selectedMethod, response);
            } else {
                showError("Lỗi thanh toán: " + response.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage(), e);
            showError("Lỗi xử lý thanh toán: " + e.getMessage());
        }
    }

    private String buildConfirmMessage(PaymentMethod method) {
        StringBuilder msg = new StringBuilder();
        msg.append("Đơn hàng: ").append(currentOrder.getOrderNumber()).append("\n");
        msg.append("Phương thức: ").append(method.getDisplayName()).append("\n");
        msg.append("Số tiền: ").append(totalLabel.getText()).append("\n\n");
        
        switch (method) {
            case COD -> msg.append("✓ Bạn sẽ thanh toán khi nhận hàng\n")
                          .append("✓ Vui lòng chuẩn bị đủ tiền mặt");
            case BANK_TRANSFER -> msg.append("✓ Vui lòng chuyển khoản theo thông tin đã hiển thị\n")
                                    .append("✓ Nội dung CK: TT ").append(currentOrder.getOrderNumber());
            case VNPAY -> msg.append("✓ Bạn sẽ được chuyển đến trang VNPay\n")
                            .append("✓ Vui lòng hoàn tất thanh toán trong 15 phút");
            case MOMO -> msg.append("✓ Bạn sẽ được chuyển đến ứng dụng MoMo\n")
                           .append("✓ Quét QR hoặc nhập OTP để xác nhận");
            case SEPAY -> msg.append("✓ Bạn sẽ được chuyển đến cổng SePay\n")
                            .append("✓ Phí thấp nhất - Chỉ 1.8%");
        }
        
        return msg.toString();
    }

    private void handleSuccessfulPayment(PaymentMethod method, PaymentResponse response) {
        if (method.isGateway() && response.getPaymentUrl() != null && !response.getPaymentUrl().isEmpty()) {
            // Gateway payment - open browser
            showGatewayPaymentInfo(method, response);
            try {
                Desktop.getDesktop().browse(new URI(response.getPaymentUrl()));
            } catch (Exception e) {
                logger.warn("Cannot open browser: {}", e.getMessage());
                // Show URL for manual copy
                showPaymentUrlDialog(response.getPaymentUrl());
            }
        } else if (method == PaymentMethod.COD) {
            // COD payment
            showCODSuccess();
        } else if (method == PaymentMethod.BANK_TRANSFER) {
            // Bank transfer
            showBankTransferSuccess();
        }
        
        // Disable payment button after successful payment
        payButton.setDisable(true);
        paymentMethodCombo.setDisable(true);
        logger.info("Payment created successfully for order {}", currentOrder.getOrderNumber());
    }

    private void showGatewayPaymentInfo(PaymentMethod method, PaymentResponse response) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chuyển đến cổng thanh toán");
        alert.setHeaderText("Đang mở " + method.getDisplayName());
        
        StringBuilder content = new StringBuilder();
        content.append("🔐 Giao dịch được bảo mật an toàn\n\n");
        content.append("Đơn hàng: ").append(currentOrder.getOrderNumber()).append("\n");
        content.append("Số tiền: ").append(totalLabel.getText()).append("\n");
        content.append("Mã giao dịch: ").append(response.getTransactionId()).append("\n\n");
        content.append("⏰ Vui lòng hoàn tất thanh toán trong 15 phút\n");
        content.append("⚠️ Không đóng trình duyệt cho đến khi hoàn tất");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void showPaymentUrlDialog(String url) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("URL Thanh toán");
        alert.setHeaderText("Trình duyệt không tự động mở");
        alert.setContentText("Vui lòng copy URL sau để thanh toán:\n\n" + url);
        
        // Make URL selectable
        TextArea textArea = new TextArea(url);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(3);
        alert.getDialogPane().setExpandableContent(textArea);
        alert.getDialogPane().setExpanded(true);
        
        alert.showAndWait();
    }

    private void showCODSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đặt hàng thành công");
        alert.setHeaderText("✓ Đơn hàng COD đã được tạo");
        
        StringBuilder content = new StringBuilder();
        content.append("Mã đơn hàng: ").append(currentOrder.getOrderNumber()).append("\n");
        content.append("Tổng tiền: ").append(totalLabel.getText()).append("\n\n");
        content.append("📦 Đơn hàng sẽ được xử lý trong 1-2 giờ\n");
        content.append("🚚 Thời gian giao hàng: 2-3 ngày\n\n");
        content.append("💵 Vui lòng chuẩn bị tiền mặt khi nhận hàng\n");
        content.append("✓ Kiểm tra hàng trước khi thanh toán cho shipper");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void showBankTransferSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chờ xác nhận thanh toán");
        alert.setHeaderText("✓ Đơn hàng đã được tạo");
        
        StringBuilder content = new StringBuilder();
        content.append("Mã đơn hàng: ").append(currentOrder.getOrderNumber()).append("\n");
        content.append("Tổng tiền: ").append(totalLabel.getText()).append("\n\n");
        content.append("🏦 THÔNG TIN CHUYỂN KHOẢN:\n");
        content.append("─────────────────────────\n");
        content.append("Ngân hàng: Vietcombank - CN Hà Nội\n");
        content.append("Số TK: 1234567890\n");
        content.append("Chủ TK: SHOP GẤU BÔNG\n");
        content.append("Nội dung: TT ").append(currentOrder.getOrderNumber()).append("\n");
        content.append("─────────────────────────\n\n");
        content.append("⏰ Sau khi chuyển khoản, đơn hàng sẽ được\n");
        content.append("   xử lý trong 1-2 giờ làm việc\n\n");
        content.append("📱 Bạn sẽ nhận được thông báo xác nhận qua SMS/Email");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        return currencyFormat.format(amount);
    }

    private String getStatusDisplayName(OrderStatus status) {
        return switch (status) {
            case CART -> "Giỏ hàng";
            case PLACED -> "Đã đặt";
            case PENDING_PAYMENT -> "Chờ thanh toán";
            case PAID -> "Đã thanh toán";
            case PACKED -> "Đã đóng gói";
            case SHIPPED -> "Đang giao";
            case DELIVERED -> "Đã giao";
            case CLOSED -> "Hoàn tất";
            case CANCELED -> "Đã hủy";
            case RMA_REQUESTED -> "Yêu cầu hoàn trả";
            case REFUNDED -> "Đã hoàn tiền";
        };
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

