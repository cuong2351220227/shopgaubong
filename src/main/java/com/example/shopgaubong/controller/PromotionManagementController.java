package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Promotion;
import com.example.shopgaubong.enums.PromotionType;
import com.example.shopgaubong.service.PromotionService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class PromotionManagementController {

    private static final Logger logger = LoggerFactory.getLogger(PromotionManagementController.class);

    @FXML private TextField txtSearch;
    @FXML private CheckBox cbActiveOnly;
    
    @FXML private TableView<Promotion> promotionTable;
    @FXML private TableColumn<Promotion, String> colCode;
    @FXML private TableColumn<Promotion, String> colName;
    @FXML private TableColumn<Promotion, String> colType;
    @FXML private TableColumn<Promotion, String> colValue;
    @FXML private TableColumn<Promotion, String> colValidPeriod;
    @FXML private TableColumn<Promotion, Boolean> colActive;

    @FXML private TextField txtCode;
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField txtDiscountValue;
    @FXML private TextField txtMinOrderValue;
    @FXML private TextField txtMaxDiscount;
    @FXML private TextField txtUsageLimit;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TextArea txtDescription;
    @FXML private CheckBox cbIsActive;

    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private PromotionService promotionService;
    private final ObservableList<Promotion> promotions = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Promotion selectedPromotion = null;

    // Constructor for dependency injection
    public PromotionManagementController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    // No-arg constructor for FXML loader
    public PromotionManagementController() {
        this.promotionService = new PromotionService();
    }

    public void setPromotionService(PromotionService promotionService) {
        this.promotionService = promotionService;
    }
    @FXML
    public void initialize() {
        setupTableColumns();
        setupTypeComboBox();
        loadPromotions();
        setupTableSelection();
        updateButtonStates();
        
        cbActiveOnly.setSelected(false);
    }

    private void setupTableColumns() {
        colCode.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCode()));

        colName.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getName()));

        colType.setCellValueFactory(cellData ->
            new SimpleStringProperty(getTypeText(cellData.getValue().getType())));

        colValue.setCellValueFactory(cellData -> {
            Promotion p = cellData.getValue();
            if (p.getType() == PromotionType.PERCENTAGE) {
                return new SimpleStringProperty(p.getDiscountValue() + "%");
            } else {
                return new SimpleStringProperty(formatCurrency(p.getDiscountValue()));
            }
        });
        
        colValidPeriod.setCellValueFactory(cellData -> {
            Promotion p = cellData.getValue();
            String period = p.getStartDate().format(dateFormatter) + " - " + 
                           p.getEndDate().format(dateFormatter);
            return new SimpleStringProperty(period);
        });
        
        colActive.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getIsActive()));

        // Style active column
        colActive.setCellFactory(col -> new TableCell<Promotion, Boolean>() {
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

        promotionTable.setItems(promotions);
    }

    private void setupTypeComboBox() {
        cbType.getItems().addAll("Phần trăm (%)", "Số tiền cố định (₫)");
        cbType.setValue("Phần trăm (%)");
        
        cbType.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Số tiền cố định (₫)".equals(newVal)) {
                txtMaxDiscount.setDisable(true);
                txtMaxDiscount.clear();
            } else {
                txtMaxDiscount.setDisable(false);
            }
        });
    }

    private void setupTableSelection() {
        promotionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedPromotion = newSelection;
            if (newSelection != null) {
                populateForm(newSelection);
            }
            updateButtonStates();
        });
    }

    private void loadPromotions() {
        try {
            List<Promotion> allPromotions = promotionService.getAllPromotions();
            promotions.clear();
            promotions.addAll(allPromotions);
            
            logger.info("Tải khuyến mãi thành công: {} khuyến mãi", promotions.size());
        } catch (Exception e) {
            logger.error("Lỗi khi tải khuyến mãi: {}", e.getMessage(), e);
            showError("Không thể tải danh sách khuyến mãi: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        boolean activeOnly = cbActiveOnly.isSelected();
        
        try {
            List<Promotion> allPromotions = promotionService.getAllPromotions();
            
            List<Promotion> filtered = allPromotions.stream()
                .filter(p -> {
                    if (activeOnly && !p.getIsActive()) {
                        return false;
                    }
                    if (!keyword.isEmpty()) {
                        return p.getName().toLowerCase().contains(keyword) ||
                               p.getCode().toLowerCase().contains(keyword);
                    }
                    return true;
                })
                .collect(Collectors.toList());
            
            promotions.clear();
            promotions.addAll(filtered);
            
            logger.info("Tìm thấy {} khuyến mãi", filtered.size());
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
            if (selectedPromotion == null) {
                createPromotion();
            } else {
                updatePromotion();
            }
            
            handleClear();
            loadPromotions();
            
        } catch (Exception e) {
            logger.error("Lỗi khi lưu khuyến mãi: {}", e.getMessage(), e);
            showError("Không thể lưu khuyến mãi: " + e.getMessage());
        }
    }

    private void createPromotion() {
        String code = txtCode.getText().trim().toUpperCase();
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();
        PromotionType type = getTypeFromText(cbType.getValue());
        BigDecimal discountValue = new BigDecimal(txtDiscountValue.getText().trim());
        BigDecimal minOrderValue = txtMinOrderValue.getText().isEmpty() ? 
                                   BigDecimal.ZERO : new BigDecimal(txtMinOrderValue.getText().trim());
        BigDecimal maxDiscount = null;
        if (type == PromotionType.PERCENTAGE && !txtMaxDiscount.getText().isEmpty()) {
            maxDiscount = new BigDecimal(txtMaxDiscount.getText().trim());
        }
        Integer usageLimit = txtUsageLimit.getText().isEmpty() ? 
                            null : Integer.parseInt(txtUsageLimit.getText().trim());
        LocalDateTime startDate = dpStartDate.getValue().atStartOfDay();
        LocalDateTime endDate = dpEndDate.getValue().atTime(23, 59, 59);
        
        Promotion promotion = promotionService.createPromotion(
            code, name, description, type, discountValue, minOrderValue, maxDiscount,
            startDate, endDate, usageLimit
        );
        
        if (!cbIsActive.isSelected()) {
            promotion.setIsActive(false);
            promotionService.updatePromotion(promotion);
        }
        
        showSuccess("Tạo khuyến mãi mới thành công!");
        logger.info("Tạo khuyến mãi mới: {}", promotion.getCode());
    }

    private void updatePromotion() {
        selectedPromotion.setName(txtName.getText().trim());
        selectedPromotion.setType(getTypeFromText(cbType.getValue()));
        selectedPromotion.setDiscountValue(new BigDecimal(txtDiscountValue.getText().trim()));
        selectedPromotion.setMinOrderValue(txtMinOrderValue.getText().isEmpty() ? 
                                          BigDecimal.ZERO : new BigDecimal(txtMinOrderValue.getText().trim()));
        
        if (selectedPromotion.getType() == PromotionType.PERCENTAGE && !txtMaxDiscount.getText().isEmpty()) {
            selectedPromotion.setMaxDiscount(new BigDecimal(txtMaxDiscount.getText().trim()));
        } else {
            selectedPromotion.setMaxDiscount(null);
        }
        
        selectedPromotion.setUsageLimit(txtUsageLimit.getText().isEmpty() ? 
                                       null : Integer.parseInt(txtUsageLimit.getText().trim()));
        selectedPromotion.setStartDate(dpStartDate.getValue().atStartOfDay());
        selectedPromotion.setEndDate(dpEndDate.getValue().atTime(23, 59, 59));
        selectedPromotion.setDescription(txtDescription.getText().trim());
        selectedPromotion.setIsActive(cbIsActive.isSelected());
        
        promotionService.updatePromotion(selectedPromotion);
        showSuccess("Cập nhật khuyến mãi thành công!");
        logger.info("Cập nhật khuyến mãi: {}", selectedPromotion.getCode());
    }

    @FXML
    private void handleDelete() {
        if (selectedPromotion == null) {
            showWarning("Vui lòng chọn khuyến mãi để xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa khuyến mãi");
        confirm.setContentText("Bạn có chắc chắn muốn xóa khuyến mãi " + selectedPromotion.getCode() + "?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                promotionService.deletePromotion(selectedPromotion.getId());
                showSuccess("Xóa khuyến mãi thành công!");
                logger.info("Xóa khuyến mãi: {}", selectedPromotion.getCode());
                
                handleClear();
                loadPromotions();
            } catch (Exception e) {
                logger.error("Lỗi khi xóa khuyến mãi: {}", e.getMessage(), e);
                showError("Không thể xóa khuyến mãi: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        selectedPromotion = null;
        promotionTable.getSelectionModel().clearSelection();
        clearForm();
        updateButtonStates();
    }

    @FXML
    private void handleRefresh() {
        loadPromotions();
        handleClear();
        showSuccess("Đã làm mới danh sách khuyến mãi!");
    }

    private void populateForm(Promotion promotion) {
        txtCode.setText(promotion.getCode());
        txtName.setText(promotion.getName());
        cbType.setValue(getTypeText(promotion.getType()));
        txtDiscountValue.setText(promotion.getDiscountValue().toString());
        txtMinOrderValue.setText(promotion.getMinOrderValue().toString());
        if (promotion.getMaxDiscount() != null) {
            txtMaxDiscount.setText(promotion.getMaxDiscount().toString());
        } else {
            txtMaxDiscount.clear();
        }
        if (promotion.getUsageLimit() != null) {
            txtUsageLimit.setText(promotion.getUsageLimit().toString());
        } else {
            txtUsageLimit.clear();
        }
        dpStartDate.setValue(promotion.getStartDate().toLocalDate());
        dpEndDate.setValue(promotion.getEndDate().toLocalDate());
        txtDescription.setText(promotion.getDescription());
        cbIsActive.setSelected(promotion.getIsActive());
        
        txtCode.setDisable(true); // Cannot change code after creation
    }

    private void clearForm() {
        txtCode.clear();
        txtName.clear();
        cbType.setValue("Phần trăm (%)");
        txtDiscountValue.clear();
        txtMinOrderValue.clear();
        txtMaxDiscount.clear();
        txtUsageLimit.clear();
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        txtDescription.clear();
        cbIsActive.setSelected(true);
        
        txtCode.setDisable(false);
    }

    private boolean validateInputs() {
        if (txtCode.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập mã khuyến mãi!");
            txtCode.requestFocus();
            return false;
        }

        if (txtName.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập tên khuyến mãi!");
            txtName.requestFocus();
            return false;
        }

        if (txtDiscountValue.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập giá trị giảm giá!");
            txtDiscountValue.requestFocus();
            return false;
        }

        try {
            BigDecimal value = new BigDecimal(txtDiscountValue.getText().trim());
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                showWarning("Giá trị giảm giá phải lớn hơn 0!");
                txtDiscountValue.requestFocus();
                return false;
            }
            
            if ("Phần trăm (%)".equals(cbType.getValue()) && value.compareTo(new BigDecimal("100")) > 0) {
                showWarning("Giá trị giảm giá phần trăm không được vượt quá 100%!");
                txtDiscountValue.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showWarning("Giá trị giảm giá không hợp lệ!");
            txtDiscountValue.requestFocus();
            return false;
        }

        if (dpStartDate.getValue() == null) {
            showWarning("Vui lòng chọn ngày bắt đầu!");
            dpStartDate.requestFocus();
            return false;
        }

        if (dpEndDate.getValue() == null) {
            showWarning("Vui lòng chọn ngày kết thúc!");
            dpEndDate.requestFocus();
            return false;
        }

        if (dpEndDate.getValue().isBefore(dpStartDate.getValue())) {
            showWarning("Ngày kết thúc phải sau ngày bắt đầu!");
            dpEndDate.requestFocus();
            return false;
        }

        return true;
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedPromotion != null;
        btnDelete.setDisable(!hasSelection);
        btnSave.setText(hasSelection ? "💾 Cập nhật" : "✚ Thêm mới");
    }

    private PromotionType getTypeFromText(String text) {
        return "Phần trăm (%)".equals(text) ? PromotionType.PERCENTAGE : PromotionType.FIXED_AMOUNT;
    }

    private String getTypeText(PromotionType type) {
        return type == PromotionType.PERCENTAGE ? "Phần trăm (%)" : "Số tiền cố định (₫)";
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
