package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Item;
import com.example.shopgaubong.entity.StockItem;
import com.example.shopgaubong.entity.Warehouse;
import com.example.shopgaubong.service.ItemService;
import com.example.shopgaubong.service.StockService;
import com.example.shopgaubong.service.WarehouseService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class StockManagementController {

    private static final Logger logger = LoggerFactory.getLogger(StockManagementController.class);

    @FXML private TextField txtSearch;
    @FXML private ComboBox<Warehouse> cboWarehouseFilter;
    @FXML private CheckBox cbLowStockOnly;
    
    @FXML private TableView<StockItem> stockTable;
    @FXML private TableColumn<StockItem, String> colWarehouse;
    @FXML private TableColumn<StockItem, String> colItemSku;
    @FXML private TableColumn<StockItem, String> colItemName;
    @FXML private TableColumn<StockItem, Integer> colOnHand;
    @FXML private TableColumn<StockItem, Integer> colReserved;
    @FXML private TableColumn<StockItem, Integer> colAvailable;
    @FXML private TableColumn<StockItem, Integer> colLowStockThreshold;
    @FXML private TableColumn<StockItem, String> colStatus;

    @FXML private ComboBox<Warehouse> cboWarehouse;
    @FXML private ComboBox<Item> cboItem;
    @FXML private TextField txtOnHand;
    @FXML private TextField txtLowStockThreshold;
    @FXML private Label lblCurrentOnHand;
    @FXML private Label lblCurrentReserved;
    @FXML private Label lblCurrentAvailable;

    @FXML private TextField txtQuantity;
    @FXML private Button btnAddStock;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private Button btnRefresh;

    private final StockService stockService = new StockService();
    private final WarehouseService warehouseService = new WarehouseService();
    private final ItemService itemService = new ItemService();
    
    private final ObservableList<StockItem> stockItemList = FXCollections.observableArrayList();
    private final ObservableList<Warehouse> warehouseList = FXCollections.observableArrayList();
    private final ObservableList<Item> itemList = FXCollections.observableArrayList();
    
    private StockItem selectedStockItem;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupListeners();
        loadWarehouses();
        loadItems();
        loadStockItems();
        
        logger.info("StockManagementController initialized");
    }

    private void setupTableColumns() {
        colWarehouse.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getWarehouse().getName()));
        
        colItemSku.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getItem().getSku()));
        
        colItemName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getItem().getName()));
        
        colOnHand.setCellValueFactory(new PropertyValueFactory<>("onHand"));
        colReserved.setCellValueFactory(new PropertyValueFactory<>("reserved"));
        
        colAvailable.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getAvailable()));
        
        colLowStockThreshold.setCellValueFactory(new PropertyValueFactory<>("lowStockThreshold"));
        
        colStatus.setCellValueFactory(cellData -> {
            StockItem stock = cellData.getValue();
            String status = stock.isLowStock() ? "⚠️ Thấp" : "✅ Bình thường";
            return new SimpleStringProperty(status);
        });

        // Apply row styling for low stock items
        stockTable.setRowFactory(tv -> new TableRow<StockItem>() {
            @Override
            protected void updateItem(StockItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.isLowStock()) {
                    setStyle("-fx-background-color: #ffebee;"); // Light red
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void setupComboBoxes() {
        // Warehouse ComboBox
        cboWarehouse.setCellFactory(param -> new ListCell<Warehouse>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (" + item.getCode() + ")");
            }
        });
        cboWarehouse.setButtonCell(new ListCell<Warehouse>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (" + item.getCode() + ")");
            }
        });

        cboWarehouseFilter.setCellFactory(param -> new ListCell<Warehouse>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "-- Tất cả kho --" : item.getName());
            }
        });
        cboWarehouseFilter.setButtonCell(new ListCell<Warehouse>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "-- Tất cả kho --" : item.getName());
            }
        });

        // Item ComboBox
        cboItem.setCellFactory(param -> new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getSku() + " - " + item.getName());
            }
        });
        cboItem.setButtonCell(new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getSku() + " - " + item.getName());
            }
        });
    }

    private void setupListeners() {
        // Table selection listener
        stockTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedStockItem = newVal;
            populateForm();
        });

        // Search listener
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterStockItems());
        
        // Filter listeners
        cboWarehouseFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterStockItems());
        cbLowStockOnly.selectedProperty().addListener((obs, oldVal, newVal) -> filterStockItems());
    }

    private void loadWarehouses() {
        try {
            List<Warehouse> warehouses = warehouseService.getAllWarehouses();
            warehouseList.setAll(warehouses);
            cboWarehouse.setItems(warehouseList);
            
            ObservableList<Warehouse> filterList = FXCollections.observableArrayList(warehouses);
            filterList.add(0, null); // Add "All" option
            cboWarehouseFilter.setItems(filterList);
            cboWarehouseFilter.getSelectionModel().selectFirst();
        } catch (Exception e) {
            logger.error("Error loading warehouses: {}", e.getMessage());
            showError("Lỗi", "Không thể tải danh sách kho: " + e.getMessage());
        }
    }

    private void loadItems() {
        try {
            List<Item> items = itemService.getAllItems();
            itemList.setAll(items);
            cboItem.setItems(itemList);
        } catch (Exception e) {
            logger.error("Error loading items: {}", e.getMessage());
            showError("Lỗi", "Không thể tải danh sách sản phẩm: " + e.getMessage());
        }
    }

    private void loadStockItems() {
        try {
            List<StockItem> stocks = stockService.getAllStockItems();
            stockItemList.setAll(stocks);
            stockTable.setItems(stockItemList);
            logger.info("Loaded {} stock items", stocks.size());
        } catch (Exception e) {
            logger.error("Error loading stock items: {}", e.getMessage());
            showError("Lỗi", "Không thể tải danh sách tồn kho: " + e.getMessage());
        }
    }

    private void filterStockItems() {
        try {
            String searchText = txtSearch.getText().toLowerCase().trim();
            Warehouse selectedWarehouse = cboWarehouseFilter.getValue();
            boolean lowStockOnly = cbLowStockOnly.isSelected();

            List<StockItem> filtered = stockItemList.stream()
                .filter(stock -> {
                    // Search filter
                    if (!searchText.isEmpty()) {
                        String itemName = stock.getItem().getName().toLowerCase();
                        String itemSku = stock.getItem().getSku().toLowerCase();
                        String warehouseName = stock.getWarehouse().getName().toLowerCase();
                        if (!itemName.contains(searchText) && 
                            !itemSku.contains(searchText) && 
                            !warehouseName.contains(searchText)) {
                            return false;
                        }
                    }

                    // Warehouse filter
                    if (selectedWarehouse != null && 
                        !stock.getWarehouse().getId().equals(selectedWarehouse.getId())) {
                        return false;
                    }

                    // Low stock filter
                    if (lowStockOnly && !stock.isLowStock()) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

            stockTable.setItems(FXCollections.observableArrayList(filtered));
        } catch (Exception e) {
            logger.error("Error filtering stock items: {}", e.getMessage());
        }
    }

    private void populateForm() {
        if (selectedStockItem != null) {
            cboWarehouse.setValue(selectedStockItem.getWarehouse());
            cboItem.setValue(selectedStockItem.getItem());
            txtOnHand.setText(String.valueOf(selectedStockItem.getOnHand()));
            txtLowStockThreshold.setText(String.valueOf(selectedStockItem.getLowStockThreshold()));
            
            lblCurrentOnHand.setText(numberFormat.format(selectedStockItem.getOnHand()));
            lblCurrentReserved.setText(numberFormat.format(selectedStockItem.getReserved()));
            lblCurrentAvailable.setText(numberFormat.format(selectedStockItem.getAvailable()));
            
            // Disable warehouse and item selection when editing
            cboWarehouse.setDisable(true);
            cboItem.setDisable(true);
            btnAddStock.setDisable(false);
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

            Warehouse warehouse = cboWarehouse.getValue();
            Item item = cboItem.getValue();
            Integer onHand = Integer.parseInt(txtOnHand.getText().trim());
            Integer lowStockThreshold = Integer.parseInt(txtLowStockThreshold.getText().trim());

            if (selectedStockItem == null) {
                // Create new stock item
                stockService.createStockItem(warehouse.getId(), item.getId(), onHand, lowStockThreshold);
                showInfo("Thành công", "Tạo tồn kho mới thành công!");
            } else {
                // Update existing stock item
                stockService.updateStockItem(selectedStockItem.getId(), onHand, lowStockThreshold);
                showInfo("Thành công", "Cập nhật tồn kho thành công!");
            }

            loadStockItems();
            clearForm();
        } catch (IllegalArgumentException e) {
            showWarning("Cảnh báo", e.getMessage());
        } catch (Exception e) {
            logger.error("Error saving stock item: {}", e.getMessage());
            showError("Lỗi", "Không thể lưu tồn kho: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddStock() {
        try {
            if (selectedStockItem == null) {
                showWarning("Cảnh báo", "Vui lòng chọn tồn kho cần nhập hàng!");
                return;
            }

            String quantityText = txtQuantity.getText().trim();
            if (quantityText.isEmpty()) {
                showWarning("Cảnh báo", "Vui lòng nhập số lượng!");
                return;
            }

            Integer quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showWarning("Cảnh báo", "Số lượng phải lớn hơn 0!");
                return;
            }

            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Xác nhận");
            confirmDialog.setHeaderText("Nhập hàng vào kho");
            confirmDialog.setContentText(String.format(
                "Bạn có chắc muốn nhập %s sản phẩm '%s' vào kho '%s'?",
                numberFormat.format(quantity),
                selectedStockItem.getItem().getName(),
                selectedStockItem.getWarehouse().getName()
            ));

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                stockService.addStock(selectedStockItem.getId(), quantity);
                showInfo("Thành công", "Nhập hàng thành công!");
                loadStockItems();
                txtQuantity.clear();
                
                // Refresh selected item
                stockTable.getSelectionModel().select(selectedStockItem);
            }
        } catch (NumberFormatException e) {
            showWarning("Cảnh báo", "Số lượng không hợp lệ!");
        } catch (Exception e) {
            logger.error("Error adding stock: {}", e.getMessage());
            showError("Lỗi", "Không thể nhập hàng: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedStockItem == null) {
            showWarning("Cảnh báo", "Vui lòng chọn tồn kho cần xóa!");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Xóa tồn kho");
        confirmDialog.setContentText(String.format(
            "Bạn có chắc muốn xóa tồn kho '%s' tại kho '%s'?",
            selectedStockItem.getItem().getName(),
            selectedStockItem.getWarehouse().getName()
        ));

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                stockService.deleteStockItem(selectedStockItem.getId());
                showInfo("Thành công", "Xóa tồn kho thành công!");
                loadStockItems();
                clearForm();
            } catch (Exception e) {
                logger.error("Error deleting stock item: {}", e.getMessage());
                showError("Lỗi", "Không thể xóa tồn kho: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    @FXML
    private void handleRefresh() {
        loadStockItems();
        showInfo("Làm mới", "Đã tải lại danh sách tồn kho!");
    }

    private void clearForm() {
        selectedStockItem = null;
        cboWarehouse.setValue(null);
        cboItem.setValue(null);
        txtOnHand.clear();
        txtLowStockThreshold.setText("10");
        txtQuantity.clear();
        lblCurrentOnHand.setText("0");
        lblCurrentReserved.setText("0");
        lblCurrentAvailable.setText("0");
        
        cboWarehouse.setDisable(false);
        cboItem.setDisable(false);
        btnAddStock.setDisable(true);
        btnDelete.setDisable(true);
        
        stockTable.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        if (cboWarehouse.getValue() == null) {
            showWarning("Cảnh báo", "Vui lòng chọn kho!");
            return false;
        }

        if (cboItem.getValue() == null) {
            showWarning("Cảnh báo", "Vui lòng chọn sản phẩm!");
            return false;
        }

        String onHandText = txtOnHand.getText().trim();
        if (onHandText.isEmpty()) {
            showWarning("Cảnh báo", "Vui lòng nhập số lượng tồn kho!");
            return false;
        }

        try {
            Integer onHand = Integer.parseInt(onHandText);
            if (onHand < 0) {
                showWarning("Cảnh báo", "Số lượng tồn kho phải >= 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarning("Cảnh báo", "Số lượng tồn kho không hợp lệ!");
            return false;
        }

        String thresholdText = txtLowStockThreshold.getText().trim();
        if (thresholdText.isEmpty()) {
            showWarning("Cảnh báo", "Vui lòng nhập ngưỡng cảnh báo!");
            return false;
        }

        try {
            Integer threshold = Integer.parseInt(thresholdText);
            if (threshold < 0) {
                showWarning("Cảnh báo", "Ngưỡng cảnh báo phải >= 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarning("Cảnh báo", "Ngưỡng cảnh báo không hợp lệ!");
            return false;
        }

        return true;
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
