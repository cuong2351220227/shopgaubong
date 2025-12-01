package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.entity.Payment;
import com.example.shopgaubong.enums.OrderStatus;
import com.example.shopgaubong.enums.PaymentStatus;
import com.example.shopgaubong.service.OrderService;
import com.example.shopgaubong.service.PaymentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PaymentManagementController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentManagementController.class);
    
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbPaymentStatus;
    @FXML private ComboBox<String> cbPaymentMethod;
    
    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, String> colOrderNumber;
    @FXML private TableColumn<Payment, String> colCustomer;
    @FXML private TableColumn<Payment, String> colPaymentMethod;
    @FXML private TableColumn<Payment, String> colAmount;
    @FXML private TableColumn<Payment, String> colPaymentStatus;
    @FXML private TableColumn<Payment, String> colOrderStatus;
    @FXML private TableColumn<Payment, String> colPaymentDate;
    
    @FXML private Label lblTotalPayments;
    @FXML private Label lblConfirmedPayments;
    @FXML private Label lblPendingPayments;
    @FXML private Label lblFailedPayments;
    
    @FXML private Label lblDetailOrderNumber;
    @FXML private Label lblDetailCustomer;
    @FXML private Label lblDetailPaymentMethod;
    @FXML private Label lblDetailAmount;
    @FXML private Label lblDetailPaymentStatus;
    @FXML private Label lblDetailOrderStatus;
    @FXML private Label lblDetailPaymentDate;
    @FXML private Label lblDetailTransactionId;
    @FXML private TextArea txtDetailNotes;
    
    private final PaymentService paymentService = new PaymentService();
    private final OrderService orderService = new OrderService();
    private ObservableList<Payment> paymentList = FXCollections.observableArrayList();
    private Payment selectedPayment;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupListeners();
        loadPayments();
        loadStatistics();
        
        logger.info("PaymentManagementController initialized");
    }

    private void setupTableColumns() {
        colOrderNumber.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getOrder().getOrderNumber()));
        
        colCustomer.setCellValueFactory(cellData -> {
            Order order = cellData.getValue().getOrder();
            String customerName = order.getCustomer().getAccountProfile() != null ?
                order.getCustomer().getAccountProfile().getFullName() :
                order.getCustomer().getUsername();
            return new SimpleStringProperty(customerName);
        });
        
        colPaymentMethod.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMethod().getDisplayName()));
        
        colAmount.setCellValueFactory(cellData -> 
            new SimpleStringProperty(currencyFormat.format(cellData.getValue().getAmount())));
        
        colPaymentStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));
        
        colOrderStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getOrder().getStatus().getDisplayName()));
        
        colPaymentDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPaidAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getPaidAt().format(dateFormatter));
            }
            return new SimpleStringProperty("-");
        });
        
        // Row styling based on payment status
        paymentTable.setRowFactory(tv -> new TableRow<Payment>() {
            @Override
            protected void updateItem(Payment payment, boolean empty) {
                super.updateItem(payment, empty);
                if (empty || payment == null) {
                    setStyle("");
                } else {
                    switch (payment.getStatus()) {
                        case COMPLETED:
                            setStyle("-fx-background-color: #e8f5e9;");
                            break;
                        case PENDING:
                            setStyle("-fx-background-color: #fff3e0;");
                            break;
                        case FAILED:
                            setStyle("-fx-background-color: #ffebee;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private void setupComboBoxes() {
        // Payment Status filter
        List<String> paymentStatuses = Arrays.asList("Tất cả");
        paymentStatuses = FXCollections.observableArrayList(paymentStatuses);
        for (PaymentStatus status : PaymentStatus.values()) {
            ((ObservableList<String>) paymentStatuses).add(status.getDisplayName());
        }
        cbPaymentStatus.setItems((ObservableList<String>) paymentStatuses);
        cbPaymentStatus.getSelectionModel().selectFirst();
        
        // Payment Method filter
        List<String> methods = Arrays.asList(
            "Tất cả",
            "Tiền mặt (COD)",
            "Chuyển khoản ngân hàng",
            "VNPay",
            "MoMo",
            "SePay"
        );
        cbPaymentMethod.setItems(FXCollections.observableArrayList(methods));
        cbPaymentMethod.getSelectionModel().selectFirst();
    }

    private void setupListeners() {
        paymentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedPayment = newSelection;
                displayPaymentDetails(newSelection);
            }
        });
    }

    private void loadPayments() {
        try {
            List<Payment> payments = paymentService.getAllPayments();
            paymentList.setAll(payments);
            paymentTable.setItems(paymentList);
            logger.info("Loaded {} payments", payments.size());
        } catch (Exception e) {
            logger.error("Error loading payments: {}", e.getMessage());
            showError("Lỗi", "Không thể tải danh sách thanh toán: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        if (lblTotalPayments != null) {
            lblTotalPayments.setText(String.valueOf(paymentList.size()));
        }
        
        if (lblConfirmedPayments != null) {
            long confirmed = paymentList.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .count();
            lblConfirmedPayments.setText(String.valueOf(confirmed));
        }
        
        if (lblPendingPayments != null) {
            long pending = paymentList.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();
            lblPendingPayments.setText(String.valueOf(pending));
        }
        
        if (lblFailedPayments != null) {
            long failed = paymentList.stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .count();
            lblFailedPayments.setText(String.valueOf(failed));
        }
    }

    private void displayPaymentDetails(Payment payment) {
        lblDetailOrderNumber.setText(payment.getOrder().getOrderNumber());
        
        String customerName = payment.getOrder().getCustomer().getAccountProfile() != null ?
            payment.getOrder().getCustomer().getAccountProfile().getFullName() :
            payment.getOrder().getCustomer().getUsername();
        lblDetailCustomer.setText(customerName);
        
        lblDetailPaymentMethod.setText(payment.getMethod().getDisplayName());
        lblDetailAmount.setText(currencyFormat.format(payment.getAmount()));
        lblDetailPaymentStatus.setText(payment.getStatus().getDisplayName());
        lblDetailOrderStatus.setText(payment.getOrder().getStatus().getDisplayName());
        
        if (payment.getPaidAt() != null) {
            lblDetailPaymentDate.setText(payment.getPaidAt().format(dateFormatter));
        } else {
            lblDetailPaymentDate.setText("-");
        }
        
        lblDetailTransactionId.setText(payment.getTransactionId() != null ? payment.getTransactionId() : "-");
        txtDetailNotes.setText(payment.getNotes() != null ? payment.getNotes() : "");
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String statusFilter = cbPaymentStatus.getValue();
        String methodFilter = cbPaymentMethod.getValue();
        
        List<Payment> filtered = paymentList.stream()
            .filter(p -> {
                // Keyword filter
                if (!keyword.isEmpty()) {
                    String orderNumber = p.getOrder().getOrderNumber().toLowerCase();
                    String customerName = p.getOrder().getCustomer().getAccountProfile() != null ?
                        p.getOrder().getCustomer().getAccountProfile().getFullName().toLowerCase() :
                        p.getOrder().getCustomer().getUsername().toLowerCase();
                    
                    if (!orderNumber.contains(keyword) && !customerName.contains(keyword)) {
                        return false;
                    }
                }
                
                // Payment status filter
                if (!statusFilter.equals("Tất cả") && !p.getStatus().getDisplayName().equals(statusFilter)) {
                    return false;
                }
                
                // Payment method filter
                if (!methodFilter.equals("Tất cả") && !p.getMethod().getDisplayName().equals(methodFilter)) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        paymentTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleRefresh() {
        txtSearch.clear();
        cbPaymentStatus.getSelectionModel().selectFirst();
        cbPaymentMethod.getSelectionModel().selectFirst();
        loadPayments();
        loadStatistics();
    }

    @FXML
    private void handleViewDetails() {
        if (selectedPayment == null) {
            showWarning("Cảnh báo", "Vui lòng chọn thanh toán để xem chi tiết!");
            return;
        }
        
        displayPaymentDetails(selectedPayment);
        showInfo("Chi tiết thanh toán", "Thông tin đã được hiển thị ở phần chi tiết bên dưới");
    }

    @FXML
    private void handleConfirmPayment() {
        if (selectedPayment == null) {
            showWarning("Cảnh báo", "Vui lòng chọn thanh toán cần xác nhận!");
            return;
        }
        
        if (selectedPayment.getStatus() == PaymentStatus.COMPLETED) {
            showWarning("Cảnh báo", "Thanh toán này đã được xác nhận trước đó!");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận");
        confirmDialog.setHeaderText("Xác nhận thanh toán");
        confirmDialog.setContentText("Bạn có chắc muốn xác nhận thanh toán cho đơn hàng " + 
                                    selectedPayment.getOrder().getOrderNumber() + "?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    selectedPayment.setStatus(PaymentStatus.COMPLETED);
                    paymentService.updatePayment(selectedPayment);
                    
                    // Update order status to PAID
                    Order order = selectedPayment.getOrder();
                    order.setStatus(OrderStatus.PAID);
                    orderService.updateOrder(order);
                    
                    loadPayments();
                    loadStatistics();
                    showInfo("Thành công", "Đã xác nhận thanh toán thành công!");
                    logger.info("Confirmed payment for order: {}", order.getOrderNumber());
                } catch (Exception e) {
                    logger.error("Error confirming payment: {}", e.getMessage());
                    showError("Lỗi", "Không thể xác nhận thanh toán: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleUpdateOrderStatus() {
        if (selectedPayment == null) {
            showWarning("Cảnh báo", "Vui lòng chọn đơn hàng cần cập nhật trạng thái!");
            return;
        }
        
        Order order = selectedPayment.getOrder();
        
        // Create dialog to select new status
        Dialog<OrderStatus> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật trạng thái đơn hàng");
        dialog.setHeaderText("Chọn trạng thái mới cho đơn hàng: " + order.getOrderNumber());
        
        ButtonType confirmButtonType = new ButtonType("Cập nhật", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        ComboBox<OrderStatus> cboStatus = new ComboBox<>();
        cboStatus.setItems(FXCollections.observableArrayList(OrderStatus.values()));
        cboStatus.setValue(order.getStatus());
        cboStatus.setPrefWidth(300);
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Trạng thái hiện tại: " + order.getStatus().getDisplayName()),
            new Label("Trạng thái mới:"),
            cboStatus
        );
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return cboStatus.getValue();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(newStatus -> {
            try {
                order.setStatus(newStatus);
                orderService.updateOrder(order);
                
                loadPayments();
                showInfo("Thành công", "Đã cập nhật trạng thái đơn hàng thành công!");
                logger.info("Updated order status to {} for order: {}", newStatus, order.getOrderNumber());
            } catch (Exception e) {
                logger.error("Error updating order status: {}", e.getMessage());
                showError("Lỗi", "Không thể cập nhật trạng thái đơn hàng: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleRefund() {
        if (selectedPayment == null) {
            showWarning("Cảnh báo", "Vui lòng chọn thanh toán cần hoàn tiền!");
            return;
        }
        
        if (selectedPayment.getStatus() != PaymentStatus.COMPLETED) {
            showWarning("Cảnh báo", "Chỉ có thể hoàn tiền cho thanh toán đã hoàn thành!");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận hoàn tiền");
        confirmDialog.setHeaderText("Hoàn tiền cho đơn hàng");
        confirmDialog.setContentText("Bạn có chắc muốn hoàn tiền " + 
                                    currencyFormat.format(selectedPayment.getAmount()) + 
                                    " cho đơn hàng " + selectedPayment.getOrder().getOrderNumber() + "?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    selectedPayment.setStatus(PaymentStatus.REFUNDED);
                    paymentService.updatePayment(selectedPayment);
                    
                    // Update order status to CANCELED
                    Order order = selectedPayment.getOrder();
                    order.setStatus(OrderStatus.CANCELED);
                    orderService.updateOrder(order);
                    
                    loadPayments();
                    loadStatistics();
                    showInfo("Thành công", "Đã hoàn tiền thành công!");
                    logger.info("Refunded payment for order: {}", order.getOrderNumber());
                } catch (Exception e) {
                    logger.error("Error refunding payment: {}", e.getMessage());
                    showError("Lỗi", "Không thể hoàn tiền: " + e.getMessage());
                }
            }
        });
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
