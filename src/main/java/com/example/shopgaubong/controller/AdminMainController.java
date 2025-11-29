package com.example.shopgaubong.controller;

import com.example.shopgaubong.service.AuthService;
import com.example.shopgaubong.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
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
        showInfo("Quản lý người dùng - Chức năng đang phát triển");
    }

    @FXML
    private void handleManageCategories() {
        showInfo("Quản lý danh mục - Chức năng đang phát triển");
    }

    @FXML
    private void handleManageItems() {
        showInfo("Quản lý sản phẩm - Chức năng đang phát triển");
    }

    @FXML
    private void handleManageWarehouse() {
        showInfo("Quản lý kho - Chức năng đang phát triển");
    }

    @FXML
    private void handleManageOrders() {
        showInfo("Quản lý đơn hàng - Chức năng đang phát triển");
    }

    @FXML
    private void handleManagePromotions() {
        showInfo("Quản lý khuyến mãi - Chức năng đang phát triển");
    }

    @FXML
    private void handleViewReports() {
        showInfo("Báo cáo - Chức năng đang phát triển");
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
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

