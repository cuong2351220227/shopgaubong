package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.entity.OrderItem;
import com.example.shopgaubong.enums.OrderStatus;
import com.example.shopgaubong.service.OrderService;
import com.example.shopgaubong.service.WarehouseService;
import javafx.beans.property.SimpleObjectProperty;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderManagementController {

    private static final Logger logger = LoggerFactory.getLogger(OrderManagementController.class);

    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private TextField txtSearch;
    
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> colOrderNumber;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, Integer> colItemCount;
    @FXML private TableColumn<Order, String> colTotal;

    @FXML private TableView<OrderItem> itemTable;
    @FXML private TableColumn<OrderItem, String> colItemName;
    @FXML private TableColumn<OrderItem, String> colSKU;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, String> colUnitPrice;
    @FXML private TableColumn<OrderItem, String> colLineTotal;

    @FXML private Label lblOrderNumber;
    @FXML private Label lblOrderDate;
    @FXML private Label lblCustomerName;
    @FXML private Label lblCustomerPhone;
    @FXML private Label lblOrderStatus;
    @FXML private Label lblShippingInfo;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTax;
    @FXML private Label lblDiscount;
    @FXML private Label lblShippingFee;
    @FXML private Label lblGrandTotal;

    @FXML private ComboBox<String> cbNewStatus;
    @FXML private Button btnUpdateStatus;
    @FXML private Button btnCancelOrder;
    @FXML private VBox detailsPanel;

    private final OrderService orderService = new OrderService();
    private final WarehouseService warehouseService = new WarehouseService();
    
    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private Order selectedOrder = null;

    @FXML
    public void initialize() {
        setupOrderTable();
        setupItemTable();
        setupStatusFilters();
        loadOrders();
        
        detailsPanel.setVisible(false);
    }

    private void setupOrderTable() {
        colOrderNumber.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getOrderNumber()));
        
        colCustomer.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCustomer().getProfile().getFullName()));
        
        colDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCreatedAt().format(dateFormatter)));
        
        colStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(getStatusText(cellData.getValue().getStatus())));
        
        colItemCount.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getOrderItems().size()));
        
        colTotal.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatCurrency(cellData.getValue().getGrandTotal())));

        // Style status column
        colStatus.setCellFactory(col -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Order order = getTableView().getItems().get(getIndex());
                    setStyle(getStatusStyle(order.getStatus()));
                }
            }
        });

        orderTable.setItems(orders);
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedOrder = newSelection;
            if (newSelection != null) {
                // Reload order with full details to avoid LazyInitializationException
                Optional<Order> orderWithDetails = orderService.getOrderWithFullDetails(newSelection.getId());
                if (orderWithDetails.isPresent()) {
                    showOrderDetails(orderWithDetails.get());
                    selectedOrder = orderWithDetails.get(); // Update selectedOrder with full details
                }
            }
            updateButtonStates();
        });
    }

    private void setupItemTable() {
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

        itemTable.setItems(orderItems);
    }

    private void setupStatusFilters() {
        cbStatusFilter.getItems().addAll(
            "Tất cả",
            "Đã đặt",
            "Chờ thanh toán",
            "Đã thanh toán",
            "Đang đóng gói",
            "Đang giao",
            "Đã giao",
            "Hoàn thành",
            "Đã hủy"
        );
        cbStatusFilter.setValue("Tất cả");

        cbNewStatus.getItems().addAll(
            "Chờ thanh toán",
            "Đã thanh toán",
            "Đang đóng gói",
            "Đang giao",
            "Đã giao",
            "Hoàn thành"
        );
    }

    private void loadOrders() {
        try {
            List<Order> allOrders = orderService.getAllOrders();
            
            // Filter out CART status
            allOrders = allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CART)
                .collect(Collectors.toList());
            
            orders.clear();
            orders.addAll(allOrders);
            
            logger.info("Tải đơn hàng thành công: {} đơn", orders.size());
        } catch (Exception e) {
            logger.error("Lỗi khi tải đơn hàng: {}", e.getMessage(), e);
            showError("Không thể tải danh sách đơn hàng: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String statusFilter = cbStatusFilter.getValue();
        
        try {
            List<Order> allOrders = orderService.getAllOrders();
            
            List<Order> filtered = allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CART)
                .filter(o -> {
                    if (!keyword.isEmpty()) {
                        return o.getOrderNumber().toLowerCase().contains(keyword) ||
                               o.getCustomer().getProfile().getFullName().toLowerCase().contains(keyword) ||
                               o.getShippingReceiverName().toLowerCase().contains(keyword);
                    }
                    return true;
                })
                .filter(o -> {
                    if (!"Tất cả".equals(statusFilter)) {
                        return getStatusText(o.getStatus()).equals(statusFilter);
                    }
                    return true;
                })
                .collect(Collectors.toList());
            
            orders.clear();
            orders.addAll(filtered);
            
            logger.info("Tìm thấy {} đơn hàng", filtered.size());
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm: {}", e.getMessage(), e);
            showError("Lỗi khi tìm kiếm: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateStatus() {
        if (selectedOrder == null) {
            showWarning("Vui lòng chọn đơn hàng!");
            return;
        }

        String newStatusText = cbNewStatus.getValue();
        if (newStatusText == null || newStatusText.isEmpty()) {
            showWarning("Vui lòng chọn trạng thái mới!");
            return;
        }

        OrderStatus newStatus = getStatusFromText(newStatusText);
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Cập nhật trạng thái đơn hàng");
        confirm.setContentText("Bạn có chắc chắn muốn chuyển trạng thái đơn hàng " + 
                              selectedOrder.getOrderNumber() + " sang: " + newStatusText + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Long warehouseId = getDefaultWarehouseId();
                    orderService.updateOrderStatus(selectedOrder.getId(), newStatus, warehouseId);
                    
                    showSuccess("Cập nhật trạng thái đơn hàng thành công!");
                    loadOrders();
                    detailsPanel.setVisible(false);
                } catch (Exception e) {
                    logger.error("Lỗi khi cập nhật trạng thái: {}", e.getMessage(), e);
                    showError("Không thể cập nhật trạng thái: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCancelOrder() {
        if (selectedOrder == null) {
            showWarning("Vui lòng chọn đơn hàng!");
            return;
        }

        if (selectedOrder.getStatus() == OrderStatus.DELIVERED || 
            selectedOrder.getStatus() == OrderStatus.CLOSED ||
            selectedOrder.getStatus() == OrderStatus.CANCELED) {
            showWarning("Không thể hủy đơn hàng này!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy đơn");
        confirm.setHeaderText("Hủy đơn hàng");
        confirm.setContentText("Bạn có chắc chắn muốn hủy đơn hàng " + 
                              selectedOrder.getOrderNumber() + "?\nHành động này không thể hoàn tác!");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Long warehouseId = getDefaultWarehouseId();
                    orderService.cancelOrder(selectedOrder.getId(), warehouseId);
                    
                    showSuccess("Hủy đơn hàng thành công!");
                    loadOrders();
                    detailsPanel.setVisible(false);
                } catch (Exception e) {
                    logger.error("Lỗi khi hủy đơn hàng: {}", e.getMessage(), e);
                    showError("Không thể hủy đơn hàng: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
        detailsPanel.setVisible(false);
        showSuccess("Đã làm mới danh sách đơn hàng!");
    }

    private void showOrderDetails(Order order) {
        // Update order information
        lblOrderNumber.setText(order.getOrderNumber());
        lblOrderDate.setText(order.getCreatedAt().format(dateFormatter));
        lblCustomerName.setText(order.getCustomer().getProfile().getFullName());
        lblCustomerPhone.setText(order.getCustomer().getProfile().getPhoneNumber());
        lblOrderStatus.setText(getStatusText(order.getStatus()));
        lblOrderStatus.setStyle("-fx-text-fill: " + getStatusColor(order.getStatus()) + "; -fx-font-weight: bold;");
        
        // Update shipping information
        String shippingInfo = String.format("%s\n%s\n%s, %s, %s\nSĐT: %s",
            order.getShippingReceiverName(),
            order.getShippingAddress(),
            order.getShippingWard(),
            order.getShippingDistrict(),
            order.getShippingCity(),
            order.getShippingPhone()
        );
        lblShippingInfo.setText(shippingInfo);
        
        // Update price summary
        lblSubtotal.setText(formatCurrency(order.getSubtotal()));
        lblTax.setText(formatCurrency(order.getTax()));
        lblDiscount.setText(formatCurrency(order.getDiscount()));
        lblShippingFee.setText(formatCurrency(order.getShippingFee()));
        lblGrandTotal.setText(formatCurrency(order.getGrandTotal()));
        
        // Load order items
        orderItems.clear();
        orderItems.addAll(order.getOrderItems());
        
        // Set appropriate new status based on current status
        setAvailableStatuses(order.getStatus());
        
        detailsPanel.setVisible(true);
    }

    private void setAvailableStatuses(OrderStatus currentStatus) {
        cbNewStatus.getItems().clear();
        
        switch (currentStatus) {
            case PLACED:
                cbNewStatus.getItems().addAll("Chờ thanh toán");
                break;
            case PENDING_PAYMENT:
                cbNewStatus.getItems().addAll("Đã thanh toán");
                break;
            case PAID:
                cbNewStatus.getItems().addAll("Đang đóng gói");
                break;
            case PACKED:
                cbNewStatus.getItems().addAll("Đang giao");
                break;
            case SHIPPED:
                cbNewStatus.getItems().addAll("Đã giao");
                break;
            case DELIVERED:
                cbNewStatus.getItems().addAll("Hoàn thành");
                break;
        }
        
        if (!cbNewStatus.getItems().isEmpty()) {
            cbNewStatus.setValue(cbNewStatus.getItems().get(0));
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedOrder != null;
        boolean canUpdate = hasSelection && 
                           selectedOrder.getStatus() != OrderStatus.CANCELED &&
                           selectedOrder.getStatus() != OrderStatus.CLOSED;
        
        btnUpdateStatus.setDisable(!canUpdate);
        btnCancelOrder.setDisable(!canUpdate);
    }

    private OrderStatus getStatusFromText(String text) {
        switch (text) {
            case "Chờ thanh toán": return OrderStatus.PENDING_PAYMENT;
            case "Đã thanh toán": return OrderStatus.PAID;
            case "Đang đóng gói": return OrderStatus.PACKED;
            case "Đang giao": return OrderStatus.SHIPPED;
            case "Đã giao": return OrderStatus.DELIVERED;
            case "Hoàn thành": return OrderStatus.CLOSED;
            default: throw new IllegalArgumentException("Invalid status text: " + text);
        }
    }

    private String getStatusText(OrderStatus status) {
        switch (status) {
            case PLACED: return "Đã đặt";
            case PENDING_PAYMENT: return "Chờ thanh toán";
            case PAID: return "Đã thanh toán";
            case PACKED: return "Đang đóng gói";
            case SHIPPED: return "Đang giao";
            case DELIVERED: return "Đã giao";
            case CLOSED: return "Hoàn thành";
            case CANCELED: return "Đã hủy";
            case RMA_REQUESTED: return "Yêu cầu trả hàng";
            case REFUNDED: return "Đã hoàn tiền";
            default: return status.name();
        }
    }

    private String getStatusColor(OrderStatus status) {
        switch (status) {
            case PLACED:
            case PENDING_PAYMENT:
                return "#ff9800";
            case PAID:
            case PACKED:
                return "#2196F3";
            case SHIPPED:
            case DELIVERED:
                return "#4CAF50";
            case CLOSED:
                return "#00BCD4";
            case CANCELED:
            case RMA_REQUESTED:
                return "#f44336";
            case REFUNDED:
                return "#9C27B0";
            default:
                return "#666";
        }
    }

    private String getStatusStyle(OrderStatus status) {
        String color = getStatusColor(status);
        return "-fx-background-color: " + color + "20; -fx-text-fill: " + color + "; " +
               "-fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 3;";
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
