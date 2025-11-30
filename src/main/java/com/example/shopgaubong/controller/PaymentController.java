package com.example.shopgaubong.controller;

import com.example.shopgaubong.dao.OrderDAO;
import com.example.shopgaubong.dto.FeeCalculation;
import com.example.shopgaubong.dto.PaymentRequest;
import com.example.shopgaubong.dto.PaymentResponse;
import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.enums.OrderStatus;
import com.example.shopgaubong.enums.PaymentMethod;
import com.example.shopgaubong.service.PaymentService;
import com.example.shopgaubong.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;

public class PaymentController {

    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> orderNumberCol;
    @FXML private TableColumn<Order, String> statusCol;
    @FXML private TableColumn<Order, BigDecimal> totalCol;

    @FXML private ComboBox<PaymentMethod> paymentMethodCombo;
    @FXML private Label subtotalLabel;
    @FXML private Label codFeeLabel;
    @FXML private Label gatewayFeeLabel;
    @FXML private Label shippingFeeLabel;
    @FXML private Label totalLabel;
    @FXML private Button payButton;
    @FXML private TextArea orderDetailsArea;

    private final PaymentService paymentService = new PaymentService();
    private final OrderDAO orderDAO = new OrderDAO();
    private Order selectedOrder;

    @FXML
    public void initialize() {
        setupOrderTable();
        setupPaymentMethodCombo();
        loadPendingOrders();

        // Disable payment button initially
        payButton.setDisable(true);

        // Listen for order selection
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedOrder = newVal;
                displayOrderDetails(newVal);
                payButton.setDisable(false);

                // Auto-select a payment method if not selected
                if (paymentMethodCombo.getValue() == null) {
                    paymentMethodCombo.setValue(PaymentMethod.COD);
                }
                updateFeeCalculation();
            }
        });

        // Update fees when payment method changes
        paymentMethodCombo.setOnAction(e -> updateFeeCalculation());
    }

    private void setupOrderTable() {
        orderNumberCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalCol.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));

        // Format currency
        totalCol.setCellFactory(col -> new TableCell<Order, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VND", item));
                }
            }
        });
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

    private void loadPendingOrders() {
        try {
            System.out.println("Loading pending orders...");

            // Check if user is logged in
            if (SessionManager.getInstance().getCurrentAccount() == null) {
                showError("Bạn chưa đăng nhập. Vui lòng đăng nhập lại.");
                System.err.println("No logged in user found");
                return;
            }

            Long customerId = SessionManager.getInstance().getCurrentAccount().getId();
            System.out.println("Customer ID: " + customerId);

            List<Order> orders = orderDAO.findByCustomerId(customerId);
            System.out.println("Total orders found: " + orders.size());

            // Filter orders that need payment (PENDING_PAYMENT, PLACED)
            List<Order> pendingOrders = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING_PAYMENT ||
                           o.getStatus() == OrderStatus.PLACED)
                .toList();

            System.out.println("Pending orders: " + pendingOrders.size());

            orderTable.setItems(FXCollections.observableArrayList(pendingOrders));

            if (pendingOrders.isEmpty()) {
                showInfo("Không có đơn hàng cần thanh toán.\n\nGợi ý: Tạo đơn hàng mới để test chức năng thanh toán.");
            }
        } catch (Exception e) {
            String errorMsg = "Lỗi tải danh sách đơn hàng: " + e.getMessage();
            showError(errorMsg);
            System.err.println("Error loading orders:");
            e.printStackTrace();
        }
    }

    private void displayOrderDetails(Order order) {
        StringBuilder details = new StringBuilder();
        details.append("Mã đơn hàng: ").append(order.getOrderNumber()).append("\n");
        details.append("Trạng thái: ").append(order.getStatus()).append("\n\n");
        details.append("Chi tiết:\n");
        details.append(String.format("Tiền hàng: %,.0f VND\n", order.getSubtotal()));
        details.append(String.format("Phí ship: %,.0f VND\n", order.getShippingFee()));
        details.append(String.format("Giảm giá: %,.0f VND\n", order.getDiscount()));
        details.append(String.format("Thuế: %,.0f VND\n", order.getTax()));
        details.append(String.format("\nTổng cộng: %,.0f VND", order.getGrandTotal()));

        orderDetailsArea.setText(details.toString());
    }

    private void updateFeeCalculation() {
        if (selectedOrder == null || paymentMethodCombo.getValue() == null) return;

        try {
            PaymentMethod method = paymentMethodCombo.getValue();
            FeeCalculation fee = paymentService.calculateFees(selectedOrder, method);

            subtotalLabel.setText(String.format("%,.0f VND", fee.getSubtotal()));
            codFeeLabel.setText(String.format("%,.0f VND", fee.getCodFee()));
            gatewayFeeLabel.setText(String.format("%,.0f VND", fee.getGatewayFee()));
            shippingFeeLabel.setText(String.format("%,.0f VND", fee.getShippingFee()));
            totalLabel.setText(String.format("%,.0f VND", fee.getGrandTotal()));

        } catch (Exception e) {
            showError("Lỗi tính phí: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayment() {
        if (selectedOrder == null) {
            showError("Vui lòng chọn đơn hàng cần thanh toán");
            return;
        }

        if (paymentMethodCombo.getValue() == null) {
            showError("Vui lòng chọn phương thức thanh toán");
            return;
        }

        // Confirm payment
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Bạn có chắc muốn thanh toán đơn hàng này?");
        confirm.setContentText(String.format("Phương thức: %s\nSố tiền: %s",
            paymentMethodCombo.getValue().getDisplayName(),
            totalLabel.getText()));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            PaymentRequest request = new PaymentRequest();
            request.setOrderId(selectedOrder.getId());
            request.setPaymentMethod(paymentMethodCombo.getValue());
            request.setAmount(selectedOrder.getGrandTotal());
            request.setReturnUrl("http://localhost:8080/payment/return");
            request.setIpAddress("127.0.0.1");

            PaymentResponse response = paymentService.createPayment(request);

            if (response.isSuccess()) {
                if (response.getPaymentUrl() != null && !response.getPaymentUrl().isEmpty()) {
                    // Gateway payment - open browser
                    showSuccess("Đang chuyển đến trang thanh toán...");
                    try {
                        Desktop.getDesktop().browse(new URI(response.getPaymentUrl()));
                    } catch (Exception e) {
                        showInfo("URL thanh toán: " + response.getPaymentUrl());
                    }
                } else {
                    // COD or Bank Transfer
                    showSuccess(response.getMessage());
                    loadPendingOrders(); // Refresh list
                }
            } else {
                showError("Lỗi thanh toán: " + response.getMessage());
            }

        } catch (Exception e) {
            showError("Lỗi xử lý thanh toán: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}

