package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Promotion;
import com.example.shopgaubong.enums.PromotionType;
import com.example.shopgaubong.service.PromotionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PromotionListController {

    private static final Logger logger = LoggerFactory.getLogger(PromotionListController.class);

    @FXML private ComboBox<String> cbFilterType;
    @FXML private Label lblPromotionCount;
    @FXML private VBox promotionsContainer;

    private final PromotionService promotionService = new PromotionService();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private List<Promotion> allPromotions;

    @FXML
    public void initialize() {
        setupFilterComboBox();
        loadPromotions();
    }

    private void setupFilterComboBox() {
        cbFilterType.getItems().addAll(
            "Tất cả",
            "Giảm theo %",
            "Giảm số tiền"
        );
        cbFilterType.setValue("Tất cả");
        cbFilterType.setOnAction(e -> filterPromotions());
    }

    private void loadPromotions() {
        try {
            allPromotions = promotionService.getActivePromotions();
            displayPromotions(allPromotions);
            updatePromotionCount(allPromotions.size());
        } catch (Exception e) {
            logger.error("Error loading promotions: {}", e.getMessage(), e);
            showError("Không thể tải danh sách khuyến mãi: " + e.getMessage());
        }
    }

    private void filterPromotions() {
        String filter = cbFilterType.getValue();
        List<Promotion> filtered;

        if ("Tất cả".equals(filter)) {
            filtered = allPromotions;
        } else if ("Giảm theo %".equals(filter)) {
            filtered = allPromotions.stream()
                .filter(p -> p.getType() == PromotionType.PERCENTAGE)
                .collect(Collectors.toList());
        } else {
            filtered = allPromotions.stream()
                .filter(p -> p.getType() == PromotionType.FIXED_AMOUNT)
                .collect(Collectors.toList());
        }

        displayPromotions(filtered);
        updatePromotionCount(filtered.size());
    }

    private void displayPromotions(List<Promotion> promotions) {
        promotionsContainer.getChildren().clear();

        if (promotions.isEmpty()) {
            Label emptyLabel = new Label("Hiện không có khuyến mãi nào");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-padding: 50;");
            promotionsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Promotion promotion : promotions) {
            VBox card = createPromotionCard(promotion);
            promotionsContainer.getChildren().add(card);
        }
    }

    private VBox createPromotionCard(Promotion promotion) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; " +
                     "-fx-border-color: #e74c3c; " +
                     "-fx-border-width: 2; " +
                     "-fx-border-radius: 8; " +
                     "-fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPadding(new Insets(20));

        // Header with code and type badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label codeLabel = new Label(promotion.getCode());
        codeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        Label typeBadge = new Label(getTypeBadge(promotion.getType()));
        typeBadge.setStyle("-fx-background-color: " + getTypeColor(promotion.getType()) + "; " +
                          "-fx-text-fill: white; " +
                          "-fx-font-size: 11px; " +
                          "-fx-padding: 4 12; " +
                          "-fx-background-radius: 12;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Discount value
        Label valueLabel = new Label(getValueDisplay(promotion));
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        header.getChildren().addAll(codeLabel, typeBadge, spacer, valueLabel);

        // Name
        Label nameLabel = new Label(promotion.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        nameLabel.setWrapText(true);

        // Description
        if (promotion.getDescription() != null && !promotion.getDescription().isEmpty()) {
            Label descLabel = new Label(promotion.getDescription());
            descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
            descLabel.setWrapText(true);
            card.getChildren().add(descLabel);
        }

        // Separator
        Separator separator = new Separator();

        // Details Grid
        GridPane details = new GridPane();
        details.setHgap(10);
        details.setVgap(8);

        int row = 0;

        // Min order value
        if (promotion.getMinOrderValue().compareTo(BigDecimal.ZERO) > 0) {
            Label minLabel = new Label("Đơn tối thiểu:");
            minLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            Label minValue = new Label(formatCurrency(promotion.getMinOrderValue()));
            minValue.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            details.add(minLabel, 0, row);
            details.add(minValue, 1, row);
            row++;
        }

        // Max discount for percentage
        if (promotion.getType() == PromotionType.PERCENTAGE && promotion.getMaxDiscountAmount() != null) {
            Label maxLabel = new Label("Giảm tối đa:");
            maxLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            Label maxValue = new Label(formatCurrency(promotion.getMaxDiscountAmount()));
            maxValue.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            details.add(maxLabel, 0, row);
            details.add(maxValue, 1, row);
            row++;
        }

        // Valid period
        Label periodLabel = new Label("Thời gian:");
        periodLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        Label periodValue = new Label(
            dateFormatter.format(promotion.getStartDate()) + " - " +
            dateFormatter.format(promotion.getEndDate())
        );
        periodValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");
        details.add(periodLabel, 0, row);
        details.add(periodValue, 1, row);
        row++;

        // Usage count
        if (promotion.getMaxUsage() != null) {
            Label usageLabel = new Label("Số lượt:");
            usageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            Label usageValue = new Label(
                promotion.getCurrentUsage() + " / " + promotion.getMaxUsage()
            );
            usageValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");
            details.add(usageLabel, 0, row);
            details.add(usageValue, 1, row);
        }

        // Copy code button
        Button copyButton = new Button("📋 Sao chép mã");
        copyButton.setStyle("-fx-background-color: #3498db; " +
                           "-fx-text-fill: white; " +
                           "-fx-font-size: 13px; " +
                           "-fx-padding: 8 20; " +
                           "-fx-cursor: hand;");
        copyButton.setMaxWidth(Double.MAX_VALUE);
        copyButton.setOnAction(e -> copyPromotionCode(promotion.getCode()));

        card.getChildren().addAll(header, nameLabel, separator, details, copyButton);

        return card;
    }

    private String getTypeBadge(PromotionType type) {
        return type == PromotionType.PERCENTAGE ? "PHẦN TRĂM" : "SỐ TIỀN";
    }

    private String getTypeColor(PromotionType type) {
        return type == PromotionType.PERCENTAGE ? "#9b59b6" : "#e67e22";
    }

    private String getValueDisplay(Promotion promotion) {
        if (promotion.getType() == PromotionType.PERCENTAGE) {
            return "-" + promotion.getDiscountValue() + "%";
        } else {
            return "-" + formatCurrency(promotion.getDiscountValue());
        }
    }

    private void copyPromotionCode(String code) {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(code);
        clipboard.setContent(content);
        showSuccess("Đã sao chép mã: " + code);
    }

    private void updatePromotionCount(int count) {
        lblPromotionCount.setText("Có " + count + " khuyến mãi");
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/customer-main.fxml"));
            Parent root = loader.load();
            promotionsContainer.getScene().setRoot(root);
        } catch (Exception e) {
            logger.error("Error navigating back: {}", e.getMessage(), e);
            showError("Không thể quay lại: " + e.getMessage());
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) amount = BigDecimal.ZERO;
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
}
