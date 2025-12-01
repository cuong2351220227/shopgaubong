# Hướng dẫn Tích hợp Chức năng Thanh toán vào Giao diện

## 📍 Vị trí Hiển thị Các Chức năng

### 1. Cấu trúc Giao diện Hiện tại

```
shopgaubong/
├── Launcher.java (Main entry point)
├── HelloApplication.java (JavaFX Application)
└── controller/
    ├── LoginController.java (Màn hình đăng nhập)
    ├── CustomerMainController.java (Giao diện Khách hàng)
    ├── AdminMainController.java (Giao diện Admin)
    └── StaffMainController.java (Giao diện Nhân viên)
```

### 2. Luồng Hoạt động

```
1. Launcher.java 
   ↓
2. HelloApplication.java (Load login-view.fxml)
   ↓
3. LoginController.java (Đăng nhập)
   ↓
4. Dựa vào Role:
   - ADMIN → AdminMainController.java (admin-main.fxml)
   - CUSTOMER → CustomerMainController.java (customer-main.fxml)
   - STAFF → StaffMainController.java (staff-main.fxml)
```

---

## 🎯 Tích hợp Chức năng Thanh toán

### A. CHO KHÁCH HÀNG (Customer)

#### 1. Tạo Controller cho Thanh toán
**File: `PaymentController.java`**

```java
package com.example.shopgaubong.controller;

import com.example.shopgaubong.dto.FeeCalculation;
import com.example.shopgaubong.dto.PaymentRequest;
import com.example.shopgaubong.dto.PaymentResponse;
import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.enums.PaymentMethod;
import com.example.shopgaubong.service.PaymentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;

public class PaymentController {
    @FXML private ComboBox<PaymentMethod> paymentMethodCombo;
    @FXML private Label subtotalLabel;
    @FXML private Label codFeeLabel;
    @FXML private Label gatewayFeeLabel;
    @FXML private Label totalLabel;
    @FXML private Button payButton;
    
    private PaymentService paymentService = new PaymentService();
    private Order currentOrder;
    
    @FXML
    public void initialize() {
        // Load payment methods
        paymentMethodCombo.setItems(FXCollections.observableArrayList(
            PaymentMethod.COD,
            PaymentMethod.BANK_TRANSFER,
            PaymentMethod.VNPAY,
            PaymentMethod.MOMO,
            PaymentMethod.SEPAY
        ));
        
        // Update fees when payment method changes
        paymentMethodCombo.setOnAction(e -> updateFeeCalculation());
    }
    
    private void updateFeeCalculation() {
        if (currentOrder == null) return;
        
        PaymentMethod method = paymentMethodCombo.getValue();
        FeeCalculation fee = paymentService.calculateFees(currentOrder, method);
        
        subtotalLabel.setText(String.format("%,.0f VND", fee.getSubtotal()));
        codFeeLabel.setText(String.format("%,.0f VND", fee.getCodFee()));
        gatewayFeeLabel.setText(String.format("%,.0f VND", fee.getGatewayFee()));
        totalLabel.setText(String.format("%,.0f VND", fee.getGrandTotal()));
    }
    
    @FXML
    private void handlePayment() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(currentOrder.getId());
        request.setPaymentMethod(paymentMethodCombo.getValue());
        request.setAmount(currentOrder.getGrandTotal());
        
        PaymentResponse response = paymentService.createPayment(request);
        
        if (response.isSuccess()) {
            if (response.getPaymentUrl() != null) {
                // Open payment URL in browser for gateway
                openInBrowser(response.getPaymentUrl());
            } else {
                showSuccess("Đặt hàng thành công!");
            }
        } else {
            showError(response.getMessage());
        }
    }
}
```

#### 2. Tạo FXML cho Thanh toán
**File: `payment-view.fxml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<VBox xmlns:fx="http://javafx.com/fxml" 
      fx:controller="com.example.shopgaubong.controller.PaymentController"
      spacing="20" padding="20">
    
    <Label text="THANH TOÁN" style="-fx-font-size: 24px; -fx-font-weight: bold;"/>
    
    <!-- Payment Method Selection -->
    <VBox spacing="10">
        <Label text="Chọn phương thức thanh toán:" style="-fx-font-weight: bold;"/>
        <ComboBox fx:id="paymentMethodCombo" prefWidth="300"/>
    </VBox>
    
    <!-- Fee Breakdown -->
    <GridPane hgap="10" vgap="10">
        <Label text="Tiền hàng:" GridPane.rowIndex="0" GridPane.columnIndex="0"/>
        <Label fx:id="subtotalLabel" text="0 VND" GridPane.rowIndex="0" GridPane.columnIndex="1"/>
        
        <Label text="Phí COD:" GridPane.rowIndex="1" GridPane.columnIndex="0"/>
        <Label fx:id="codFeeLabel" text="0 VND" GridPane.rowIndex="1" GridPane.columnIndex="1"/>
        
        <Label text="Phí Gateway:" GridPane.rowIndex="2" GridPane.columnIndex="0"/>
        <Label fx:id="gatewayFeeLabel" text="0 VND" GridPane.rowIndex="2" GridPane.columnIndex="1"/>
        
        <Separator GridPane.rowIndex="3" GridPane.columnSpan="2"/>
        
        <Label text="TỔNG CỘNG:" GridPane.rowIndex="4" GridPane.columnIndex="0" 
               style="-fx-font-weight: bold; -fx-font-size: 16px;"/>
        <Label fx:id="totalLabel" text="0 VND" GridPane.rowIndex="4" GridPane.columnIndex="1"
               style="-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: red;"/>
    </GridPane>
    
    <!-- Pay Button -->
    <Button fx:id="payButton" text="THANH TOÁN" onAction="#handlePayment"
            style="-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;"
            prefWidth="200" prefHeight="40"/>
</VBox>
```

#### 3. Cập nhật CustomerMainController.java

```java
// Thêm vào CustomerMainController.java

@FXML
private void handleCheckout() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/payment-view.fxml"));
        BorderPane paymentView = loader.load();
        contentPane.setCenter(paymentView);
    } catch (Exception e) {
        showError("Không thể mở màn hình thanh toán: " + e.getMessage());
    }
}

@FXML
private void handleViewOrders() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/order-list-view.fxml"));
        BorderPane orderView = loader.load();
        contentPane.setCenter(orderView);
    } catch (Exception e) {
        showError("Không thể mở danh sách đơn hàng: " + e.getMessage());
    }
}
```

---

### B. CHO ADMIN (Administrator)

#### 1. Tạo Controller Quản lý Thanh toán
**File: `PaymentManagementController.java`**

```java
package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Payment;
import com.example.shopgaubong.entity.Refund;
import com.example.shopgaubong.service.PaymentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PaymentManagementController {
    @FXML private TableView<Payment> paymentTable;
    @FXML private TableView<Refund> refundTable;
    
    private PaymentService paymentService = new PaymentService();
    
    @FXML
    public void initialize() {
        setupPaymentTable();
        setupRefundTable();
        loadPendingRefunds();
    }
    
    private void setupRefundTable() {
        // Setup columns for refund table
    }
    
    private void loadPendingRefunds() {
        ObservableList<Refund> refunds = FXCollections.observableArrayList(
            paymentService.getPendingRefunds()
        );
        refundTable.setItems(refunds);
    }
    
    @FXML
    private void handleApproveRefund() {
        Refund selected = refundTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean success = paymentService.approveRefund(selected.getId());
            if (success) {
                showSuccess("Đã duyệt hoàn tiền!");
                loadPendingRefunds();
            }
        }
    }
    
    @FXML
    private void handleRejectRefund() {
        Refund selected = refundTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Từ chối hoàn tiền");
            dialog.setHeaderText("Nhập lý do từ chối:");
            dialog.showAndWait().ifPresent(reason -> {
                boolean success = paymentService.rejectRefund(selected.getId(), reason);
                if (success) {
                    showSuccess("Đã từ chối yêu cầu hoàn tiền!");
                    loadPendingRefunds();
                }
            });
        }
    }
}
```

#### 2. Cập nhật AdminMainController.java

```java
// Thêm vào AdminMainController.java

@FXML
private void handleManagePayments() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/payment-management-view.fxml"));
        BorderPane paymentMgmt = loader.load();
        contentPane.setCenter(paymentMgmt);
    } catch (Exception e) {
        showError("Không thể mở quản lý thanh toán: " + e.getMessage());
    }
}

@FXML
private void handleManageRefunds() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/refund-management-view.fxml"));
        BorderPane refundMgmt = loader.load();
        contentPane.setCenter(refundMgmt);
    } catch (Exception e) {
        showError("Không thể mở quản lý hoàn tiền: " + e.getMessage());
    }
}
```

---

### C. CHO STAFF (Nhân viên)

#### Cập nhật StaffMainController.java

```java
// Thêm vào StaffMainController.java

@FXML
private void handleConfirmPayment() {
    // Confirm COD payment when staff receives money
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/confirm-payment-view.fxml"));
        BorderPane confirmView = loader.load();
        contentPane.setCenter(confirmView);
    } catch (Exception e) {
        showError("Không thể mở xác nhận thanh toán: " + e.getMessage());
    }
}

@FXML
private void handleViewPayments() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/payment-list-view.fxml"));
        BorderPane paymentList = loader.load();
        contentPane.setCenter(paymentList);
    } catch (Exception e) {
        showError("Không thể mở danh sách thanh toán: " + e.getMessage());
    }
}
```

---

## 📋 Cập nhật Menu trong FXML

### 1. customer-main.fxml
Thêm menu items:
```xml
<MenuItem text="Thanh toán" onAction="#handleCheckout"/>
<MenuItem text="Lịch sử đơn hàng" onAction="#handleViewOrders"/>
<MenuItem text="Yêu cầu hoàn tiền" onAction="#handleRequestRefund"/>
```

### 2. admin-main.fxml
Thêm menu items:
```xml
<Menu text="Thanh toán">
    <MenuItem text="Quản lý thanh toán" onAction="#handleManagePayments"/>
    <MenuItem text="Quản lý hoàn tiền" onAction="#handleManageRefunds"/>
    <MenuItem text="Báo cáo thanh toán" onAction="#handlePaymentReports"/>
</Menu>
```

### 3. staff-main.fxml
Thêm menu items:
```xml
<MenuItem text="Xác nhận thanh toán COD" onAction="#handleConfirmPayment"/>
<MenuItem text="Danh sách thanh toán" onAction="#handleViewPayments"/>
```

---

## 🚀 Các Bước Tích hợp

### Bước 1: Tạo các Controller
1. ✅ `PaymentController.java` - Cho khách hàng thanh toán
2. ✅ `PaymentManagementController.java` - Admin quản lý
3. ✅ `RefundManagementController.java` - Admin duyệt hoàn tiền
4. ✅ `ConfirmPaymentController.java` - Staff xác nhận COD

### Bước 2: Tạo các FXML
1. ✅ `payment-view.fxml` - Màn hình thanh toán
2. ✅ `payment-management-view.fxml` - Quản lý thanh toán
3. ✅ `refund-management-view.fxml` - Quản lý hoàn tiền
4. ✅ `order-list-view.fxml` - Danh sách đơn hàng

### Bước 3: Cập nhật Controllers hiện có
1. ✅ Thêm methods vào `CustomerMainController`
2. ✅ Thêm methods vào `AdminMainController`
3. ✅ Thêm methods vào `StaffMainController`

### Bước 4: Cập nhật FXML menu
1. ✅ Cập nhật `customer-main.fxml`
2. ✅ Cập nhật `admin-main.fxml`
3. ✅ Cập nhật `staff-main.fxml`

---

## 📱 Sơ đồ Luồng Thanh toán

```
┌─────────────────────────────────────────┐
│  CUSTOMER: Xem giỏ hàng                 │
└───────────────┬─────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│  Click "Thanh toán"                     │
│  → CustomerMainController               │
│     .handleCheckout()                   │
└───────────────┬─────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│  Load payment-view.fxml                 │
│  → PaymentController                    │
└───────────────┬─────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│  1. Chọn phương thức (COD/VNPAY/MOMO)  │
│  2. Hiển thị chi tiết phí               │
│  3. Click "Thanh toán"                  │
└───────────────┬─────────────────────────┘
                ↓
         ┌──────┴──────┐
         ↓             ↓
    ┌────────┐    ┌────────┐
    │  COD   │    │Gateway │
    └────┬───┘    └───┬────┘
         ↓            ↓
    Chờ nhận      Open URL
      hàng        Browser
         ↓            ↓
    Staff xác    Callback
     nhận         handler
```

---

## 💡 Ví dụ Sử dụng

### Tạo thanh toán từ Controller:
```java
PaymentService paymentService = new PaymentService();

// Tính phí trước
FeeCalculation fee = paymentService.calculateFees(order, PaymentMethod.VNPAY);
System.out.println("Tổng: " + fee.getGrandTotal());

// Tạo thanh toán
PaymentRequest request = new PaymentRequest();
request.setOrderId(orderId);
request.setPaymentMethod(PaymentMethod.VNPAY);
request.setAmount(order.getGrandTotal());
request.setReturnUrl("http://yourapp.com/payment/return");

PaymentResponse response = paymentService.createPayment(request);
if (response.isSuccess()) {
    // Mở URL thanh toán
    Desktop.getDesktop().browse(new URI(response.getPaymentUrl()));
}
```

---

## 📝 Tổng kết

**Các chức năng thanh toán sẽ được hiển thị tại:**

1. **Khách hàng (Customer)**:
   - Menu "Thanh toán" trong customer-main.fxml
   - Hiển thị trong contentPane của CustomerMainController
   - File: payment-view.fxml

2. **Admin (Administrator)**:
   - Menu "Quản lý thanh toán" trong admin-main.fxml
   - Hiển thị trong contentPane của AdminMainController
   - Files: payment-management-view.fxml, refund-management-view.fxml

3. **Staff (Nhân viên)**:
   - Menu "Xác nhận thanh toán" trong staff-main.fxml
   - Hiển thị trong contentPane của StaffMainController
   - File: confirm-payment-view.fxml

**Cấu trúc BorderPane:**
```
┌─────────────────────────────────────┐
│  Top: Menu Bar                      │
├─────────────────────────────────────┤
│  Left: Navigation (optional)        │
│  ┌───────────────────────────────┐ │
│  │  Center: contentPane          │ │
│  │  (Load payment views here)    │ │
│  └───────────────────────────────┘ │
├─────────────────────────────────────┤
│  Bottom: Status Bar (optional)      │
└─────────────────────────────────────┘
```

Tất cả các view thanh toán sẽ được load vào `contentPane` (BorderPane center) của các MainController tương ứng!

