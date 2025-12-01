package com.example.shopgaubong.controller;

import com.example.shopgaubong.service.AuthService;
import com.example.shopgaubong.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminMainController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private BorderPane contentPane;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        String fullName = authService.getCurrentAccount().getProfile().getFullName();
        welcomeLabel.setText("Xin chào, " + fullName + " (Quản trị viên)");
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        navigateToLogin();
    }

    @FXML
    private void handleManageUsers() {
        loadView("/com/example/shopgaubong/user-management-view.fxml", "Quản lý người dùng");
    }

    @FXML
    private void handleManageCategories() {
        loadView("/com/example/shopgaubong/category-management-view.fxml", "Quản lý danh mục");
    }

    @FXML
    private void handleManageItems() {
        loadView("/com/example/shopgaubong/item-management-view.fxml", "Quản lý sản phẩm");
    }

    @FXML
    private void handleManageWarehouse() {
        loadView("/com/example/shopgaubong/warehouse-management-view.fxml", "Quản lý kho");
    }

    @FXML
    private void handleManageStock() {
        loadView("/com/example/shopgaubong/stock-management-view.fxml", "Quản lý tồn kho");
    }

    @FXML
    private void handleManageOrders() {
        loadView("/com/example/shopgaubong/order-management-view.fxml", "Quản lý đơn hàng");
    }

    @FXML
    private void handleManagePromotions() {
        loadView("/com/example/shopgaubong/promotion-management-view.fxml", "Quản lý khuyến mãi");
    }

    @FXML
    private void handleViewReports() {
        showInfo("Báo cáo - Chức năng đang phát triển");
    }

    @FXML
    private void handleManagePayments() {
        // Load refund management view
        loadView("/com/example/shopgaubong/payment-management-view.fxml", "Quản lý hoàn tiền");
    }

    @FXML
    private void handleManageRefunds() {
        // Load refund management view
        loadView("/com/example/shopgaubong/refund-management-view.fxml", "Quản lý hoàn tiền");
    }

    /**
     * Load a view into the center content pane
     */
    private void loadView(String fxmlPath, String title) {
        try {
            System.out.println("Admin loading view: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            if (loader.getLocation() == null) {
                showError("Không tìm thấy file FXML: " + fxmlPath);
                System.err.println("FXML file not found: " + fxmlPath);
                return;
            }

            VBox view = loader.load();
            contentPane.setCenter(view);
            System.out.println("View loaded successfully: " + title);
        } catch (Exception e) {
            String errorMsg = "Không thể mở " + title + ": " + e.getMessage();
            showError(errorMsg);
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void navigateToLogin() {
        try {
            Stage stage = (Stage) contentPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 400, 500);
            stage.setScene(scene);
            stage.setTitle("UCOP - Đăng Nhập");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}

