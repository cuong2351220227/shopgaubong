package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Warehouse;
import com.example.shopgaubong.service.WarehouseService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WarehouseManagementController {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseManagementController.class);

    @FXML private TextField txtSearch;
    @FXML private CheckBox cbActiveOnly;
    
    @FXML private TableView<Warehouse> warehouseTable;
    @FXML private TableColumn<Warehouse, String> colName;
    @FXML private TableColumn<Warehouse, String> colCode;
    @FXML private TableColumn<Warehouse, String> colAddress;
    @FXML private TableColumn<Warehouse, String> colPhone;
    @FXML private TableColumn<Warehouse, String> colManager;
    @FXML private TableColumn<Warehouse, Boolean> colActive;

    @FXML private TextField txtName;
    @FXML private TextField txtCode;
    @FXML private TextField txtAddress;
    @FXML private TextField txtCity;
    @FXML private TextField txtDistrict;
    @FXML private TextField txtWard;
    @FXML private TextField txtPhone;
    @FXML private TextField txtManagerName;
    @FXML private TextField txtManagerPhone;
    @FXML private TextField txtManagerEmail;
    @FXML private TextArea txtDescription;
    @FXML private CheckBox cbIsActive;

    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private final WarehouseService warehouseService = new WarehouseService();
    private final ObservableList<Warehouse> warehouses = FXCollections.observableArrayList();
    private Warehouse selectedWarehouse = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadWarehouses();
        setupTableSelection();
        updateButtonStates();
        
        cbActiveOnly.setSelected(true);
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
        
        colCode.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCode()));
        
        colAddress.setCellValueFactory(cellData -> {
            Warehouse w = cellData.getValue();
            String address = w.getAddress();
            if (w.getCity() != null) {
                address += ", " + w.getCity();
            }
            return new SimpleStringProperty(address);
        });
        
        colPhone.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPhone()));
        
        colManager.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getManagerName()));
        
        colActive.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getIsActive()));

        // Style active column
        colActive.setCellFactory(col -> new TableCell<Warehouse, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✓ Hoạt động" : "✗ Ngừng");
                    setStyle(item ? "-fx-text-fill: #4CAF50; -fx-font-weight: bold;" :
                                  "-fx-text-fill: #f44336; -fx-font-weight: bold;");
                }
            }
        });

        warehouseTable.setItems(warehouses);
    }

    private void setupTableSelection() {
        warehouseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedWarehouse = newSelection;
            if (newSelection != null) {
                populateForm(newSelection);
            }
            updateButtonStates();
        });
    }

    private void loadWarehouses() {
        try {
            List<Warehouse> allWarehouses = warehouseService.getAllWarehouses();
            warehouses.clear();
            warehouses.addAll(allWarehouses);
            
            logger.info("Tải kho thành công: {} kho", warehouses.size());
        } catch (Exception e) {
            logger.error("Lỗi khi tải kho: {}", e.getMessage(), e);
            showError("Không thể tải danh sách kho: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        boolean activeOnly = cbActiveOnly.isSelected();
        
        try {
            List<Warehouse> allWarehouses = warehouseService.getAllWarehouses();
            
            List<Warehouse> filtered = allWarehouses.stream()
                .filter(w -> {
                    if (activeOnly && !w.getIsActive()) {
                        return false;
                    }
                    if (!keyword.isEmpty()) {
                        return w.getName().toLowerCase().contains(keyword) ||
                               w.getCode().toLowerCase().contains(keyword) ||
                               (w.getAddress() != null && w.getAddress().toLowerCase().contains(keyword));
                    }
                    return true;
                })
                .collect(Collectors.toList());
            
            warehouses.clear();
            warehouses.addAll(filtered);
            
            logger.info("Tìm thấy {} kho", filtered.size());
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm: {}", e.getMessage(), e);
            showError("Lỗi khi tìm kiếm: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            if (selectedWarehouse == null) {
                // Create new warehouse
                createWarehouse();
            } else {
                // Update existing warehouse
                updateWarehouse();
            }
            
            handleClear();
            loadWarehouses();
            
        } catch (Exception e) {
            logger.error("Lỗi khi lưu kho: {}", e.getMessage(), e);
            showError("Không thể lưu kho: " + e.getMessage());
        }
    }

    private void createWarehouse() {
        Warehouse warehouse = new Warehouse();
        populateWarehouse(warehouse);
        
        warehouseService.createWarehouse(warehouse);
        showSuccess("Tạo kho mới thành công!");
        logger.info("Tạo kho mới: {}", warehouse.getName());
    }

    private void updateWarehouse() {
        populateWarehouse(selectedWarehouse);
        
        warehouseService.updateWarehouse(selectedWarehouse);
        showSuccess("Cập nhật kho thành công!");
        logger.info("Cập nhật kho: {}", selectedWarehouse.getName());
    }

    private void populateWarehouse(Warehouse warehouse) {
        warehouse.setName(txtName.getText().trim());
        warehouse.setCode(txtCode.getText().trim().toUpperCase());
        warehouse.setAddress(txtAddress.getText().trim());
        warehouse.setCity(txtCity.getText().trim());
        warehouse.setDistrict(txtDistrict.getText().trim());
        warehouse.setWard(txtWard.getText().trim());
        warehouse.setPhone(txtPhone.getText().trim());
        warehouse.setManagerName(txtManagerName.getText().trim());
        warehouse.setManagerPhone(txtManagerPhone.getText().trim());
        warehouse.setManagerEmail(txtManagerEmail.getText().trim());
        warehouse.setDescription(txtDescription.getText().trim());
        warehouse.setIsActive(cbIsActive.isSelected());
    }

    @FXML
    private void handleDelete() {
        if (selectedWarehouse == null) {
            showWarning("Vui lòng chọn kho để xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa kho");
        confirm.setContentText("Bạn có chắc chắn muốn xóa kho " + selectedWarehouse.getName() + "?\n" +
                              "Hành động này không thể hoàn tác!");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                warehouseService.deleteWarehouse(selectedWarehouse.getId());
                showSuccess("Xóa kho thành công!");
                logger.info("Xóa kho: {}", selectedWarehouse.getName());
                
                handleClear();
                loadWarehouses();
            } catch (Exception e) {
                logger.error("Lỗi khi xóa kho: {}", e.getMessage(), e);
                showError("Không thể xóa kho: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        selectedWarehouse = null;
        warehouseTable.getSelectionModel().clearSelection();
        clearForm();
        updateButtonStates();
    }

    @FXML
    private void handleRefresh() {
        loadWarehouses();
        handleClear();
        showSuccess("Đã làm mới danh sách kho!");
    }

    private void populateForm(Warehouse warehouse) {
        txtName.setText(warehouse.getName());
        txtCode.setText(warehouse.getCode());
        txtAddress.setText(warehouse.getAddress());
        txtCity.setText(warehouse.getCity());
        txtDistrict.setText(warehouse.getDistrict());
        txtWard.setText(warehouse.getWard());
        txtPhone.setText(warehouse.getPhone());
        txtManagerName.setText(warehouse.getManagerName());
        txtManagerPhone.setText(warehouse.getManagerPhone());
        txtManagerEmail.setText(warehouse.getManagerEmail());
        txtDescription.setText(warehouse.getDescription());
        cbIsActive.setSelected(warehouse.getIsActive());
    }

    private void clearForm() {
        txtName.clear();
        txtCode.clear();
        txtAddress.clear();
        txtCity.clear();
        txtDistrict.clear();
        txtWard.clear();
        txtPhone.clear();
        txtManagerName.clear();
        txtManagerPhone.clear();
        txtManagerEmail.clear();
        txtDescription.clear();
        cbIsActive.setSelected(true);
    }

    private boolean validateInputs() {
        if (txtName.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập tên kho!");
            txtName.requestFocus();
            return false;
        }

        if (txtCode.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập mã kho!");
            txtCode.requestFocus();
            return false;
        }

        if (txtAddress.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập địa chỉ!");
            txtAddress.requestFocus();
            return false;
        }

        if (txtPhone.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập số điện thoại!");
            txtPhone.requestFocus();
            return false;
        }

        // Validate phone format
        String phone = txtPhone.getText().trim();
        if (!phone.matches("^[0-9]{10,11}$")) {
            showWarning("Số điện thoại không hợp lệ! Vui lòng nhập 10-11 chữ số.");
            txtPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedWarehouse != null;
        btnDelete.setDisable(!hasSelection);
        btnSave.setText(hasSelection ? "💾 Cập nhật" : "✚ Thêm mới");
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
