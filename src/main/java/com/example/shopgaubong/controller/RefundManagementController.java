package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Refund;
import com.example.shopgaubong.enums.RefundStatus;
import com.example.shopgaubong.service.PaymentService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class RefundManagementController {

    @FXML private TableView<Refund> refundTable;
    @FXML private TableColumn<Refund, String> refundNumberCol;
    @FXML private TableColumn<Refund, BigDecimal> amountCol;
    @FXML private TableColumn<Refund, RefundStatus> statusCol;
    @FXML private TableColumn<Refund, LocalDateTime> createdAtCol;

    @FXML private TextArea reasonArea;
    @FXML private TextArea adminNotesArea;
    @FXML private Label paymentInfoLabel;
    @FXML private Label customerInfoLabel;

    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button refreshButton;

    private final PaymentService paymentService = new PaymentService();

    @FXML
    public void initialize() {
        setupTable();
        loadPendingRefunds();

        // Listen for refund selection
        refundTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayRefundDetails(newVal);
                boolean isPending = newVal.getStatus() == RefundStatus.PENDING;
                approveButton.setDisable(!isPending);
                rejectButton.setDisable(!isPending);
            }
        });
    }

    private void setupTable() {
        refundNumberCol.setCellValueFactory(new PropertyValueFactory<>("refundNumber"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Format currency
        amountCol.setCellFactory(col -> new TableCell<Refund, BigDecimal>() {
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

        // Format status with color
        statusCol.setCellFactory(col -> new TableCell<Refund, RefundStatus>() {
            @Override
            protected void updateItem(RefundStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getDisplayName());
                    switch (item) {
                        case PENDING -> setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        case APPROVED -> setStyle("-fx-text-fill: blue;");
                        case COMPLETED -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        case REJECTED -> setStyle("-fx-text-fill: red;");
                        case FAILED -> setStyle("-fx-text-fill: darkred;");
                        default -> setStyle("");
                    }
                }
            }
        });

        // Format datetime
        createdAtCol.setCellFactory(col -> new TableCell<Refund, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString().replace('T', ' '));
                }
            }
        });
    }

    private void loadPendingRefunds() {
        try {
            System.out.println("Loading pending refunds...");
            var refunds = paymentService.getPendingRefunds();
            System.out.println("Pending refunds found: " + refunds.size());

            refundTable.setItems(FXCollections.observableArrayList(refunds));

            if (refunds.isEmpty()) {
                showInfo("Không có yêu cầu hoàn tiền đang chờ xử lý.\n\nGợi ý: Yêu cầu hoàn tiền sẽ hiển thị ở đây khi khách hàng tạo yêu cầu.");
            }
        } catch (Exception e) {
            String errorMsg = "Lỗi tải danh sách hoàn tiền: " + e.getMessage();
            showError(errorMsg);
            System.err.println("Error loading refunds:");
            e.printStackTrace();
        }
    }

    private void displayRefundDetails(Refund refund) {
        reasonArea.setText(refund.getReason());
        adminNotesArea.setText(refund.getAdminNotes() != null ? refund.getAdminNotes() : "");

        // Display payment info
        StringBuilder paymentInfo = new StringBuilder();
        paymentInfo.append("Mã thanh toán: ").append(refund.getPayment().getTransactionId()).append("\n");
        paymentInfo.append("Phương thức: ").append(refund.getPayment().getMethod().getDisplayName()).append("\n");
        paymentInfo.append(String.format("Số tiền gốc: %,.0f VND\n", refund.getPayment().getAmount()));
        paymentInfo.append(String.format("Số tiền hoàn: %,.0f VND\n", refund.getAmount()));
        paymentInfo.append(String.format("Phí hoàn tiền: %,.0f VND\n", refund.getRefundFee()));
        paymentInfo.append(String.format("Số tiền thực nhận: %,.0f VND", refund.getNetRefundAmount()));
        paymentInfoLabel.setText(paymentInfo.toString());

        // Display customer info
        String customerName = refund.getPayment().getOrder().getCustomer().getProfile().getFullName();
        String customerPhone = refund.getPayment().getOrder().getCustomer().getProfile().getPhone();
        customerInfoLabel.setText("Khách hàng: " + customerName + "\nSĐT: " + customerPhone);
    }

    @FXML
    private void handleApprove() {
        Refund selected = refundTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn yêu cầu hoàn tiền");
            return;
        }

        // Confirm
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận duyệt hoàn tiền");
        confirm.setHeaderText("Bạn có chắc muốn duyệt yêu cầu hoàn tiền này?");
        confirm.setContentText(String.format("Mã: %s\nSố tiền: %,.0f VND",
            selected.getRefundNumber(), selected.getAmount()));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean success = paymentService.approveRefund(selected.getId());
            if (success) {
                showSuccess("Đã duyệt yêu cầu hoàn tiền thành công!");
                loadPendingRefunds();
            } else {
                showError("Không thể duyệt yêu cầu hoàn tiền");
            }
        } catch (Exception e) {
            showError("Lỗi duyệt hoàn tiền: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReject() {
        Refund selected = refundTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn yêu cầu hoàn tiền");
            return;
        }

        // Ask for reason
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Từ chối hoàn tiền");
        dialog.setHeaderText("Nhập lý do từ chối:");
        dialog.setContentText("Lý do:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) {
            showError("Vui lòng nhập lý do từ chối");
            return;
        }

        try {
            boolean success = paymentService.rejectRefund(selected.getId(), result.get());
            if (success) {
                showSuccess("Đã từ chối yêu cầu hoàn tiền!");
                loadPendingRefunds();
            } else {
                showError("Không thể từ chối yêu cầu hoàn tiền");
            }
        } catch (Exception e) {
            showError("Lỗi từ chối hoàn tiền: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadPendingRefunds();
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

