package com.example.shopgaubong.controller;

import com.example.shopgaubong.service.ReportService;
import com.example.shopgaubong.enums.OrderStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller cho Report View - Báo cáo và Thống kê
 */
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService = new ReportService();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // Filter controls
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cbTimePeriod;

    // Statistics labels
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblWeekRevenue;
    @FXML private Label lblMonthRevenue;
    @FXML private Label lblTodayOrders;
    @FXML private Label lblAverageOrderValue;
    @FXML private Label lblCompletionRate;
    @FXML private Label lblLowStockCount;

    // Charts
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private PieChart orderStatusChart;
    @FXML private BarChart<String, Number> topProductsChart;

    // Low Stock Table
    @FXML private TableView<LowStockItem> lowStockTable;
    @FXML private TableColumn<LowStockItem, String> colLowStockSKU;
    @FXML private TableColumn<LowStockItem, String> colLowStockName;
    @FXML private TableColumn<LowStockItem, String> colLowStockWarehouse;
    @FXML private TableColumn<LowStockItem, Integer> colLowStockQuantity;
    @FXML private TableColumn<LowStockItem, Integer> colLowStockReorderPoint;

    private ObservableList<LowStockItem> lowStockItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupDatePickers();
        setupTimePeriodComboBox();
        setupLowStockTable();
        loadDashboardData();
        logger.info("ReportController initialized");
    }

    private void setupDatePickers() {
        // Set default date range to last 30 days
        dpEndDate.setValue(LocalDate.now());
        dpStartDate.setValue(LocalDate.now().minusDays(30));
    }

    private void setupTimePeriodComboBox() {
        cbTimePeriod.getItems().addAll(
                "Hôm nay",
                "Tuần này",
                "Tháng này",
                "Năm này",
                "30 ngày qua"
        );
        cbTimePeriod.setValue("30 ngày qua");
        cbTimePeriod.setOnAction(event -> handleTimePeriodChange());
    }

    private void setupLowStockTable() {
        colLowStockSKU.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colLowStockName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLowStockWarehouse.setCellValueFactory(new PropertyValueFactory<>("warehouseName"));
        colLowStockQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colLowStockReorderPoint.setCellValueFactory(new PropertyValueFactory<>("reorderPoint"));

        // Style quantity column with color
        colLowStockQuantity.setCellFactory(col -> new TableCell<LowStockItem, Integer>() {
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(quantity));
                    setStyle("-fx-text-fill: #ff5252; -fx-font-weight: bold;");
                }
            }
        });

        lowStockTable.setItems(lowStockItems);
    }

    @FXML
    private void handleTimePeriodChange() {
        String period = cbTimePeriod.getValue();
        if (period == null) return;

        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period) {
            case "Hôm nay":
                startDate = endDate;
                break;
            case "Tuần này":
                startDate = endDate.minusDays(endDate.getDayOfWeek().getValue() - 1);
                break;
            case "Tháng này":
                startDate = endDate.withDayOfMonth(1);
                break;
            case "Năm này":
                startDate = endDate.withDayOfYear(1);
                break;
            case "30 ngày qua":
            default:
                startDate = endDate.minusDays(30);
                break;
        }

        dpStartDate.setValue(startDate);
        dpEndDate.setValue(endDate);
        handleFilter();
    }

    @FXML
    private void handleFilter() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        if (startDate == null || endDate == null) {
            showError("Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc");
            return;
        }

        if (startDate.isAfter(endDate)) {
            showError("Ngày bắt đầu không thể sau ngày kết thúc");
            return;
        }

        loadFilteredData(startDate, endDate);
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
        showInfo("Đã làm mới dữ liệu báo cáo");
    }

    private void loadDashboardData() {
        try {
            // Load statistics
            Map<String, Object> stats = reportService.getDashboardStats();

            // Update labels
            lblTodayRevenue.setText(formatCurrency((BigDecimal) stats.get("todayRevenue")));
            lblWeekRevenue.setText(formatCurrency((BigDecimal) stats.get("weekRevenue")));
            lblMonthRevenue.setText(formatCurrency((BigDecimal) stats.get("monthRevenue")));
            lblTodayOrders.setText(String.valueOf(stats.get("todayOrders")));
            lblAverageOrderValue.setText(formatCurrency((BigDecimal) stats.get("averageOrderValue")));

            // Calculate completion rate
            double completionRate = reportService.getOrderCompletionRate(
                    LocalDate.now().withDayOfMonth(1).atStartOfDay(),
                    LocalDateTime.now()
            );
            lblCompletionRate.setText(String.format("%.1f%%", completionRate));

            // Load charts with default date range
            loadRevenueChart(dpStartDate.getValue(), dpEndDate.getValue());
            loadOrderStatusChart();
            loadTopProductsChart();
            loadLowStockData();

            logger.info("Dashboard data loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading dashboard data: {}", e.getMessage(), e);
            showError("Lỗi khi tải dữ liệu: " + e.getMessage());
        }
    }

    private void loadFilteredData(LocalDate startDate, LocalDate endDate) {
        try {
            // Load filtered charts
            loadRevenueChart(startDate, endDate);
            loadOrderStatusChart(); // This shows all orders, not filtered by date
            loadTopProductsChart(); // This shows all-time top products

            // Update some filtered statistics
            BigDecimal periodRevenue = reportService.getTotalRevenue(
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59)
            );
            long periodOrders = reportService.getTotalOrders(
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59)
            );
            BigDecimal avgOrderValue = reportService.getAverageOrderValue(
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59)
            );
            double completionRate = reportService.getOrderCompletionRate(
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59)
            );

            // Show info alert with filtered results
            showInfo(String.format(
                    "Kết quả lọc từ %s đến %s:\n" +
                    "Doanh thu: %s\n" +
                    "Số đơn hàng: %d\n" +
                    "Giá trị TB: %s\n" +
                    "Tỷ lệ hoàn thành: %.1f%%",
                    startDate, endDate,
                    formatCurrency(periodRevenue),
                    periodOrders,
                    formatCurrency(avgOrderValue),
                    completionRate
            ));

            logger.info("Filtered data loaded for period: {} to {}", startDate, endDate);
        } catch (Exception e) {
            logger.error("Error loading filtered data: {}", e.getMessage(), e);
            showError("Lỗi khi tải dữ liệu lọc: " + e.getMessage());
        }
    }

    private void loadRevenueChart(LocalDate startDate, LocalDate endDate) {
        try {
            Map<LocalDate, BigDecimal> dailyRevenue = reportService.getDailyRevenue(startDate, endDate);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu");

            dailyRevenue.forEach((date, revenue) -> {
                series.getData().add(new XYChart.Data<>(
                        date.toString(),
                        revenue.doubleValue()
                ));
            });

            revenueChart.getData().clear();
            revenueChart.getData().add(series);

            logger.debug("Revenue chart loaded with {} data points", dailyRevenue.size());
        } catch (Exception e) {
            logger.error("Error loading revenue chart: {}", e.getMessage(), e);
        }
    }

    private void loadOrderStatusChart() {
        try {
            Map<OrderStatus, Long> orderCounts = reportService.getOrderCountByStatus();

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            orderCounts.forEach((status, count) -> {
                String label = getStatusDisplayName(status) + " (" + count + ")";
                pieData.add(new PieChart.Data(label, count));
            });

            orderStatusChart.setData(pieData);

            logger.debug("Order status chart loaded with {} statuses", orderCounts.size());
        } catch (Exception e) {
            logger.error("Error loading order status chart: {}", e.getMessage(), e);
        }
    }

    private void loadTopProductsChart() {
        try {
            List<Map<String, Object>> topProducts = reportService.getTopSellingProducts(10);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Số lượng bán");

            for (Map<String, Object> product : topProducts) {
                String name = (String) product.get("name");
                Long totalSold = (Long) product.get("totalSold");

                // Shorten name if too long
                if (name.length() > 20) {
                    name = name.substring(0, 17) + "...";
                }

                series.getData().add(new XYChart.Data<>(name, totalSold));
            }

            topProductsChart.getData().clear();
            topProductsChart.getData().add(series);

            logger.debug("Top products chart loaded with {} products", topProducts.size());
        } catch (Exception e) {
            logger.error("Error loading top products chart: {}", e.getMessage(), e);
        }
    }

    private void loadLowStockData() {
        try {
            List<Map<String, Object>> lowStockData = reportService.getLowStockItems();

            lowStockItems.clear();
            for (Map<String, Object> item : lowStockData) {
                lowStockItems.add(new LowStockItem(
                        (String) item.get("sku"),
                        (String) item.get("name"),
                        (String) item.get("warehouseName"),
                        (Integer) item.get("quantity"),
                        (Integer) item.get("reorderPoint")
                ));
            }

            lblLowStockCount.setText("(" + lowStockItems.size() + " sản phẩm)");

            logger.debug("Low stock data loaded with {} items", lowStockItems.size());
        } catch (Exception e) {
            logger.error("Error loading low stock data: {}", e.getMessage(), e);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        return currencyFormat.format(amount);
    }

    private String getStatusDisplayName(OrderStatus status) {
        return switch (status) {
            case PLACED -> "Đã đặt";
            case PENDING_PAYMENT -> "Chờ thanh toán";
            case PAID -> "Đã thanh toán";
            case PACKED -> "Đã đóng gói";
            case SHIPPED -> "Đang giao";
            case DELIVERED -> "Đã giao";
            case CLOSED -> "Hoàn tất";
            case CANCELED -> "Đã hủy";
            case REFUNDED -> "Đã hoàn tiền";
            case RMA_REQUESTED -> "Yêu cầu hoàn trả";
            default -> status.name();
        };
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
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

    /**
     * Inner class for Low Stock Table items
     */
    public static class LowStockItem {
        private final String sku;
        private final String name;
        private final String warehouseName;
        private final Integer quantity;
        private final Integer reorderPoint;

        public LowStockItem(String sku, String name, String warehouseName, 
                           Integer quantity, Integer reorderPoint) {
            this.sku = sku;
            this.name = name;
            this.warehouseName = warehouseName;
            this.quantity = quantity;
            this.reorderPoint = reorderPoint;
        }

        public String getSku() { return sku; }
        public String getName() { return name; }
        public String getWarehouseName() { return warehouseName; }
        public Integer getQuantity() { return quantity; }
        public Integer getReorderPoint() { return reorderPoint; }
    }
}
