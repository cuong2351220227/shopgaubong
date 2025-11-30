package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Order;
import com.example.shopgaubong.entity.Shipment;
import com.example.shopgaubong.enums.ShipmentStatus;
import com.example.shopgaubong.service.OrderService;
import com.example.shopgaubong.service.ShipmentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShipmentManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentManagementController.class);

    @FXML private TextField txtSearch;
    @FXML private ComboBox<ShipmentStatus> cboStatusFilter;
    @FXML private ComboBox<String> cbStatusFilter; // For new FXML
    @FXML private ComboBox<String> cbCarrierFilter; // For new FXML
    @FXML private CheckBox cbOverdueOnly;
    
    @FXML private TableView<Shipment> shipmentTable;
    @FXML private TableColumn<Shipment, String> colTrackingNumber;
    @FXML private TableColumn<Shipment, String> colOrderNumber;
    @FXML private TableColumn<Shipment, String> colCarrier;
    @FXML private TableColumn<Shipment, String> colReceiver; // For new FXML
    @FXML private TableColumn<Shipment, String> colPhone; // For new FXML
    @FXML private TableColumn<Shipment, String> colStatus;
    @FXML private TableColumn<Shipment, String> colShippedDate;
    @FXML private TableColumn<Shipment, String> colEstimatedDelivery;
    @FXML private TableColumn<Shipment, String> colCreatedAt; // For new FXML

    @FXML private TextField txtTrackingNumber;
    @FXML private ComboBox<String> cboCarrier;
    @FXML private ComboBox<Order> cboOrder;
    @FXML private DatePicker dpEstimatedDelivery;
    @FXML private ComboBox<ShipmentStatus> cboStatus;
    @FXML private TextArea txtNotes;

    @FXML private Label lblOrderNumber;
    @FXML private Label lblCustomerName;
    @FXML private Label lblShippingAddress;
    @FXML private Label lblCurrentStatus;
    @FXML private Label lblShippedDate;
    @FXML private Label lblDeliveredDate;

    @FXML private Button btnSave;
    @FXML private Button btnUpdateStatus;
    @FXML private Button btnViewDetails; // For new FXML
    @FXML private Button btnTrackShipment; // For new FXML
    @FXML private Button btnCancelShipment; // For new FXML
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private Button btnRefresh;

    // Labels for statistics in new FXML
    @FXML private Label lblShipmentCount;
    @FXML private Label lblPendingCount;
    @FXML private Label lblInTransitCount;
    @FXML private Label lblDeliveredCount;
    @FXML private Label lblFailedCount;

    private final ShipmentService shipmentService = new ShipmentService();
    private final OrderService orderService = new OrderService();
    
    private ObservableList<Shipment> shipmentList = FXCollections.observableArrayList();
    private ObservableList<Order> orderList = FXCollections.observableArrayList();
    private Shipment selectedShipment;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupListeners();
        loadOrders();
        loadShipments();
        loadStatistics();
        
        logger.info("ShipmentManagementController initialized");
    }

    private void setupTableColumns() {
        colTrackingNumber.setCellValueFactory(new PropertyValueFactory<>("trackingNumber"));
        
        colOrderNumber.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getOrder().getOrderNumber()));
        
        colCarrier.setCellValueFactory(new PropertyValueFactory<>("carrier"));
        
        // New FXML columns
        if (colReceiver != null) {
            colReceiver.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getReceiverName()));
        }
        
        if (colPhone != null) {
            colPhone.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getReceiverPhone()));
        }
        
        colStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));
        
        // Old FXML has colShippedDate, new FXML doesn't
        if (colShippedDate != null) {
            colShippedDate.setCellValueFactory(cellData -> 
                new SimpleStringProperty(
                    cellData.getValue().getShippedAt() != null ?
                    cellData.getValue().getShippedAt().format(dateFormatter) : "Chưa gửi"
                ));
        }
        
        colEstimatedDelivery.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getEstimatedDeliveryAt() != null ?
                cellData.getValue().getEstimatedDeliveryAt().format(dateFormatter) : "N/A"
            ));
        
        if (colCreatedAt != null) {
            colCreatedAt.setCellValueFactory(cellData -> 
                new SimpleStringProperty(
                    cellData.getValue().getCreatedAt() != null ?
                    cellData.getValue().getCreatedAt().format(dateFormatter) : ""
                ));
        }

        // Apply row styling based on status and overdue
        shipmentTable.setRowFactory(tv -> new TableRow<Shipment>() {
            @Override
            protected void updateItem(Shipment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    // Check if overdue
                    boolean isOverdue = item.getEstimatedDeliveryAt() != null &&
                                      item.getStatus() != ShipmentStatus.DELIVERED &&
                                      LocalDateTime.now().isAfter(item.getEstimatedDeliveryAt());
                    
                    if (isOverdue) {
                        setStyle("-fx-background-color: #ffebee;"); // Light red for overdue
                    } else {
                        switch (item.getStatus()) {
                            case PENDING:
                                setStyle("-fx-background-color: #fff3e0;"); // Light orange
                                break;
                            case PICKED_UP:
                            case IN_TRANSIT:
                                setStyle("-fx-background-color: #e3f2fd;"); // Light blue
                                break;
                            case OUT_FOR_DELIVERY:
                                setStyle("-fx-background-color: #f3e5f5;"); // Light purple
                                break;
                            case DELIVERED:
                                setStyle("-fx-background-color: #e8f5e9;"); // Light green
                                break;
                            case FAILED:
                            case RETURNED:
                                setStyle("-fx-background-color: #ffcdd2;"); // Red
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            }
        });
    }

    private void setupComboBoxes() {
        // Status filter ComboBox (old style)
        if (cboStatusFilter != null) {
            List<ShipmentStatus> filterStatuses = Arrays.asList(
                null, // All
                ShipmentStatus.PENDING,
                ShipmentStatus.PICKED_UP,
                ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.OUT_FOR_DELIVERY,
                ShipmentStatus.DELIVERED,
                ShipmentStatus.FAILED,
                ShipmentStatus.RETURNED
            );
            
            cboStatusFilter.setItems(FXCollections.observableArrayList(filterStatuses));
            cboStatusFilter.setCellFactory(param -> new ListCell<ShipmentStatus>() {
                @Override
                protected void updateItem(ShipmentStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "-- Tất cả trạng thái --" : item.getDisplayName());
                }
            });
            cboStatusFilter.setButtonCell(new ListCell<ShipmentStatus>() {
                @Override
                protected void updateItem(ShipmentStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "-- Tất cả trạng thái --" : item.getDisplayName());
                }
            });
            cboStatusFilter.getSelectionModel().selectFirst();
        }
        
        // New FXML status filter (String-based)
        if (cbStatusFilter != null) {
            List<String> statusOptions = new ArrayList<>();
            statusOptions.add("Tất cả");
            for (ShipmentStatus status : ShipmentStatus.values()) {
                statusOptions.add(status.getDisplayName());
            }
            cbStatusFilter.setItems(FXCollections.observableArrayList(statusOptions));
            cbStatusFilter.getSelectionModel().selectFirst();
        }
        
        // New FXML carrier filter
        if (cbCarrierFilter != null) {
            List<String> carriers = Arrays.asList(
                "Tất cả",
                "Giao hàng nhanh", 
                "Giao hàng tiết kiệm",
                "VNPost",
                "J&T Express",
                "Viettel Post",
                "Ninja Van"
            );
            cbCarrierFilter.setItems(FXCollections.observableArrayList(carriers));
            cbCarrierFilter.getSelectionModel().selectFirst();
        }

        // Status ComboBox (for creating/updating)
        if (cboStatus != null) {
            List<ShipmentStatus> allStatuses = Arrays.asList(ShipmentStatus.values());
            cboStatus.setItems(FXCollections.observableArrayList(allStatuses));
            cboStatus.setCellFactory(param -> new ListCell<ShipmentStatus>() {
                @Override
                protected void updateItem(ShipmentStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getDisplayName());
                }
            });
            cboStatus.setButtonCell(new ListCell<ShipmentStatus>() {
                @Override
                protected void updateItem(ShipmentStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getDisplayName());
                }
            });
        }

        // Carrier ComboBox
        if (cboCarrier != null) {
            List<String> carriers = Arrays.asList(
                "Giao hàng nhanh", "Giao hàng tiết kiệm", "VNPost", 
                "J&T Express", "BEST Express", "Ninja Van", "Viettel Post"
            );
            cboCarrier.setItems(FXCollections.observableArrayList(carriers));
        }

        // Order ComboBox
        if (cboOrder != null) {
            cboOrder.setCellFactory(param -> new ListCell<Order>() {
                @Override
                protected void updateItem(Order item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(item.getOrderNumber() + " - " + 
                               (item.getCustomer().getAccountProfile() != null ?
                                item.getCustomer().getAccountProfile().getFullName() :
                                item.getCustomer().getUsername()));
                    }
                }
            });
            cboOrder.setButtonCell(new ListCell<Order>() {
                @Override
                protected void updateItem(Order item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(item.getOrderNumber() + " - " + 
                               (item.getCustomer().getAccountProfile() != null ?
                                item.getCustomer().getAccountProfile().getFullName() :
                                item.getCustomer().getUsername()));
                    }
                }
            });
        }
    }

    private void setupListeners() {
        // Table selection listener
        shipmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedShipment = newVal;
            populateForm();
        });

        // Search listener
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterShipments());
        
        // Filter listeners
        cboStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterShipments());
        cbOverdueOnly.selectedProperty().addListener((obs, oldVal, newVal) -> filterShipments());
    }

    private void loadOrders() {
        try {
            // Load only orders that are PAID, PACKED, or SHIPPED (eligible for shipment)
            List<Order> orders = orderService.getAllOrders().stream()
                .filter(order -> order.getStatus().name().matches("PAID|PACKED|SHIPPED"))
                .collect(Collectors.toList());
            
            orderList.setAll(orders);
            cboOrder.setItems(orderList);
        } catch (Exception e) {
            logger.error("Error loading orders: {}", e.getMessage());
            showError("Lỗi", "Không thể tải danh sách đơn hàng: " + e.getMessage());
        }
    }

    private void loadShipments() {
        try {
            List<Shipment> shipments = shipmentService.getAllShipments();
            shipmentList.setAll(shipments);
            shipmentTable.setItems(shipmentList);
            loadStatistics();
            logger.info("Loaded {} shipments", shipments.size());
        } catch (Exception e) {
            logger.error("Error loading shipments: {}", e.getMessage());
            showError("Lỗi", "Không thể tải danh sách vận đơn: " + e.getMessage());
        }
    }

    private void filterShipments() {
        try {
            String searchText = txtSearch.getText().toLowerCase().trim();
            ShipmentStatus statusFilter = cboStatusFilter.getValue();
            boolean overdueOnly = cbOverdueOnly.isSelected();

            List<Shipment> filtered = shipmentList.stream()
                .filter(shipment -> {
                    // Search filter
                    if (!searchText.isEmpty()) {
                        String trackingNumber = shipment.getTrackingNumber() != null ?
                            shipment.getTrackingNumber().toLowerCase() : "";
                        String orderNumber = shipment.getOrder().getOrderNumber().toLowerCase();
                        String carrier = shipment.getCarrier() != null ?
                            shipment.getCarrier().toLowerCase() : "";
                        
                        if (!trackingNumber.contains(searchText) && 
                            !orderNumber.contains(searchText) &&
                            !carrier.contains(searchText)) {
                            return false;
                        }
                    }

                    // Status filter
                    if (statusFilter != null && shipment.getStatus() != statusFilter) {
                        return false;
                    }

                    // Overdue filter
                    if (overdueOnly) {
                        boolean isOverdue = shipment.getEstimatedDeliveryAt() != null &&
                                          shipment.getStatus() != ShipmentStatus.DELIVERED &&
                                          LocalDateTime.now().isAfter(shipment.getEstimatedDeliveryAt());
                        if (!isOverdue) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

            shipmentTable.setItems(FXCollections.observableArrayList(filtered));
        } catch (Exception e) {
            logger.error("Error filtering shipments: {}", e.getMessage());
        }
    }

    private void populateForm() {
        if (selectedShipment != null) {
            txtTrackingNumber.setText(selectedShipment.getTrackingNumber());
            cboCarrier.setValue(selectedShipment.getCarrier());
            cboOrder.setValue(selectedShipment.getOrder());
            cboStatus.setValue(selectedShipment.getStatus());
            txtNotes.setText(selectedShipment.getNotes() != null ? selectedShipment.getNotes() : "");
            
            if (selectedShipment.getEstimatedDeliveryAt() != null) {
                dpEstimatedDelivery.setValue(selectedShipment.getEstimatedDeliveryAt().toLocalDate());
            }
            
            // Display order info
            Order order = selectedShipment.getOrder();
            lblOrderNumber.setText(order.getOrderNumber());
            
            String customerName = order.getCustomer().getAccountProfile() != null ?
                order.getCustomer().getAccountProfile().getFullName() :
                order.getCustomer().getUsername();
            lblCustomerName.setText(customerName);
            
            String fullAddress = String.format("%s, %s, %s, %s",
                order.getShippingAddress() != null ? order.getShippingAddress() : "",
                order.getShippingWard() != null ? order.getShippingWard() : "",
                order.getShippingDistrict() != null ? order.getShippingDistrict() : "",
                order.getShippingCity() != null ? order.getShippingCity() : ""
            );
            lblShippingAddress.setText(fullAddress);
            
            // Display shipment status info
            lblCurrentStatus.setText(selectedShipment.getStatus().getDisplayName());
            lblCurrentStatus.setStyle(getStatusStyle(selectedShipment.getStatus()));
            
            lblShippedDate.setText(selectedShipment.getShippedAt() != null ?
                selectedShipment.getShippedAt().format(dateFormatter) : "Chưa gửi");
            
            lblDeliveredDate.setText(selectedShipment.getDeliveredAt() != null ?
                selectedShipment.getDeliveredAt().format(dateFormatter) : "Chưa giao");
            
            // Disable order selection when editing
            cboOrder.setDisable(true);
            btnUpdateStatus.setDisable(false);
            btnDelete.setDisable(false);
        } else {
            clearForm();
        }
    }

    @FXML
    private void handleSave() {
        try {
            if (!validateInput()) {
                return;
            }

            String trackingNumber = txtTrackingNumber.getText().trim();
            String carrier = cboCarrier.getValue();
            Order order = cboOrder.getValue();
            LocalDate estimatedDate = dpEstimatedDelivery.getValue();
            LocalDateTime estimatedDelivery = estimatedDate != null ?
                estimatedDate.atStartOfDay() : null;

            if (selectedShipment == null) {
                // Create new shipment
                shipmentService.createShipment(
                    order.getId(),
                    trackingNumber,
                    carrier,
                    estimatedDelivery
                );
                showInfo("Thành công", "Tạo vận đơn mới thành công!");
            } else {
                // Update existing shipment
                String notes = txtNotes.getText().trim();
                shipmentService.updateShipment(
                    selectedShipment.getId(),
                    trackingNumber,
                    carrier,
                    estimatedDelivery,
                    notes
                );
                showInfo("Thành công", "Cập nhật vận đơn thành công!");
            }

            loadShipments();
            clearForm();
        } catch (Exception e) {
            logger.error("Error saving shipment: {}", e.getMessage());
            showError("Lỗi", "Không thể lưu vận đơn: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateStatus() {
        if (selectedShipment == null) {
            showWarning("Cảnh báo", "Vui lòng chọn vận đơn cần cập nhật!");
            return;
        }

        ShipmentStatus newStatus = cboStatus.getValue();
        if (newStatus == null) {
            showWarning("Cảnh báo", "Vui lòng chọn trạng thái mới!");
            return;
        }

        if (newStatus == selectedShipment.getStatus()) {
            showWarning("Cảnh báo", "Trạng thái mới giống trạng thái hiện tại!");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận");
        confirmDialog.setHeaderText("Cập nhật trạng thái vận đơn");
        confirmDialog.setContentText(String.format(
            "Bạn có chắc muốn thay đổi trạng thái vận đơn '%s' từ '%s' sang '%s'?",
            selectedShipment.getTrackingNumber(),
            selectedShipment.getStatus().getDisplayName(),
            newStatus.getDisplayName()
        ));

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String note = txtNotes.getText().trim();
                LocalDateTime deliveredAt = newStatus == ShipmentStatus.DELIVERED ?
                    LocalDateTime.now() : null;
                
                shipmentService.updateShipmentStatus(
                    selectedShipment.getId(),
                    newStatus,
                    note,
                    deliveredAt
                );
                showInfo("Thành công", "Cập nhật trạng thái vận đơn thành công!");
                loadShipments();
                
                // Reselect the shipment
                shipmentTable.getSelectionModel().select(selectedShipment);
            } catch (Exception e) {
                logger.error("Error updating shipment status: {}", e.getMessage());
                showError("Lỗi", "Không thể cập nhật trạng thái: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedShipment == null) {
            showWarning("Cảnh báo", "Vui lòng chọn vận đơn cần xóa!");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Xóa vận đơn");
        confirmDialog.setContentText(String.format(
            "Bạn có chắc muốn xóa vận đơn '%s'?",
            selectedShipment.getTrackingNumber()
        ));

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                shipmentService.deleteShipment(selectedShipment.getId());
                showInfo("Thành công", "Xóa vận đơn thành công!");
                loadShipments();
                clearForm();
            } catch (Exception e) {
                logger.error("Error deleting shipment: {}", e.getMessage());
                showError("Lỗi", "Không thể xóa vận đơn: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
        loadShipments();
        showInfo("Làm mới", "Đã tải lại danh sách vận đơn!");
    }

    private void clearForm() {
        selectedShipment = null;
        txtTrackingNumber.clear();
        cboCarrier.setValue(null);
        cboOrder.setValue(null);
        cboStatus.setValue(ShipmentStatus.PENDING);
        dpEstimatedDelivery.setValue(null);
        txtNotes.clear();
        
        lblOrderNumber.setText("");
        lblCustomerName.setText("");
        lblShippingAddress.setText("");
        lblCurrentStatus.setText("");
        lblShippedDate.setText("");
        lblDeliveredDate.setText("");
        
        cboOrder.setDisable(false);
        btnUpdateStatus.setDisable(true);
        btnDelete.setDisable(true);
        
        shipmentTable.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        if (cboOrder.getValue() == null) {
            showWarning("Cảnh báo", "Vui lòng chọn đơn hàng!");
            return false;
        }

        String trackingNumber = txtTrackingNumber.getText().trim();
        if (trackingNumber.isEmpty()) {
            showWarning("Cảnh báo", "Vui lòng nhập mã vận đơn!");
            return false;
        }

        if (cboCarrier.getValue() == null || cboCarrier.getValue().isEmpty()) {
            showWarning("Cảnh báo", "Vui lòng chọn đơn vị vận chuyển!");
            return false;
        }

        return true;
    }

    private String getStatusStyle(ShipmentStatus status) {
        String baseStyle = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 5 10; -fx-border-radius: 5; -fx-background-radius: 5; ";
        switch (status) {
            case PENDING:
                return baseStyle + "-fx-background-color: #fff3e0; -fx-text-fill: #ff9800;";
            case PICKED_UP:
            case IN_TRANSIT:
                return baseStyle + "-fx-background-color: #e3f2fd; -fx-text-fill: #2196F3;";
            case OUT_FOR_DELIVERY:
                return baseStyle + "-fx-background-color: #f3e5f5; -fx-text-fill: #9c27b0;";
            case DELIVERED:
                return baseStyle + "-fx-background-color: #e8f5e9; -fx-text-fill: #4CAF50;";
            case FAILED:
            case RETURNED:
                return baseStyle + "-fx-background-color: #ffcdd2; -fx-text-fill: #f44336;";
            default:
                return baseStyle + "-fx-background-color: #f5f5f5; -fx-text-fill: #333;";
        }
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

    // Thêm các method cho FXML mới
    @FXML
    private void handleCreateShipment() {
        showInfo("Tạo vận đơn mới", "Chức năng đang được phát triển");
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        
        // Get filters from new FXML combo boxes if available
        String statusFilter = cbStatusFilter != null && cbStatusFilter.getValue() != null ? 
                             cbStatusFilter.getValue() : "Tất cả";
        String carrierFilter = cbCarrierFilter != null && cbCarrierFilter.getValue() != null ? 
                              cbCarrierFilter.getValue() : "Tất cả";
        
        // Filter shipments based on keyword, status, and carrier
        List<Shipment> filtered = shipmentList.stream()
            .filter(s -> {
                // Keyword filter
                if (!keyword.isEmpty()) {
                    String search = keyword.toLowerCase();
                    boolean matchesKeyword = 
                        s.getTrackingNumber().toLowerCase().contains(search) ||
                        s.getOrder().getOrderNumber().toLowerCase().contains(search) ||
                        s.getCarrier().toLowerCase().contains(search) ||
                        (s.getReceiverName() != null && s.getReceiverName().toLowerCase().contains(search));
                    if (!matchesKeyword) return false;
                }
                
                // Status filter
                if (!statusFilter.equals("Tất cả") && !s.getStatus().getDisplayName().equals(statusFilter)) {
                    return false;
                }
                
                // Carrier filter
                if (!carrierFilter.equals("Tất cả") && !s.getCarrier().equals(carrierFilter)) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        shipmentTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleViewDetails() {
        if (selectedShipment != null) {
            showShipmentDetails();
        } else {
            showWarning("Cảnh báo", "Vui lòng chọn vận đơn để xem chi tiết");
        }
    }

    @FXML
    private void handleTrackShipment() {
        if (selectedShipment != null) {
            showInfo("Theo dõi vận đơn", "Mã vận đơn: " + selectedShipment.getTrackingNumber() + "\nChức năng theo dõi chi tiết đang được phát triển");
        } else {
            showWarning("Cảnh báo", "Vui lòng chọn vận đơn cần theo dõi");
        }
    }

    @FXML
    private void handleCancelShipment() {
        if (selectedShipment != null) {
            handleDelete();
        } else {
            showWarning("Cảnh báo", "Vui lòng chọn vận đơn cần hủy");
        }
    }

    private void showShipmentDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Mã vận đơn: ").append(selectedShipment.getTrackingNumber()).append("\n");
        details.append("Đơn hàng: ").append(selectedShipment.getOrder().getOrderNumber()).append("\n");
        details.append("Nhà vận chuyển: ").append(selectedShipment.getCarrier()).append("\n");
        details.append("Trạng thái: ").append(selectedShipment.getStatus().getDisplayName()).append("\n");
        if (selectedShipment.getShippedAt() != null) {
            details.append("Ngày gửi: ").append(selectedShipment.getShippedAt().format(dateFormatter)).append("\n");
        }
        if (selectedShipment.getEstimatedDeliveryAt() != null) {
            details.append("Dự kiến giao: ").append(selectedShipment.getEstimatedDeliveryAt().format(dateFormatter)).append("\n");
        }
        if (selectedShipment.getNotes() != null && !selectedShipment.getNotes().isEmpty()) {
            details.append("Ghi chú: ").append(selectedShipment.getNotes()).append("\n");
        }
        
        showInfo("Chi tiết vận đơn", details.toString());
    }
    
    private void loadStatistics() {
        if (lblShipmentCount != null) {
            lblShipmentCount.setText(String.valueOf(shipmentList.size()));
        }
        
        if (lblPendingCount != null) {
            long pendingCount = shipmentList.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.PENDING || s.getStatus() == ShipmentStatus.PREPARING)
                .count();
            lblPendingCount.setText(String.valueOf(pendingCount));
        }
        
        if (lblInTransitCount != null) {
            long inTransitCount = shipmentList.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT || 
                           s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
                .count();
            lblInTransitCount.setText(String.valueOf(inTransitCount));
        }
        
        if (lblDeliveredCount != null) {
            long deliveredCount = shipmentList.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                .count();
            lblDeliveredCount.setText(String.valueOf(deliveredCount));
        }
        
        if (lblFailedCount != null) {
            long failedCount = shipmentList.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.FAILED || 
                           s.getStatus() == ShipmentStatus.CANCELLED)
                .count();
            lblFailedCount.setText(String.valueOf(failedCount));
        }
    }
}
