package com.example.shopgaubong.controller;

import com.example.shopgaubong.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class CustomerMainController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private BorderPane contentPane;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        String fullName = authService.getCurrentAccount().getProfile().getFullName();
        welcomeLabel.setText("Xin chào, " + fullName);
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        navigateToLogin();
    }

    @FXML
    private void handleBrowseProducts() {
        showInfo("Xem sản phẩm - Chức năng đang phát triển");
    }

    @FXML
    private void handleViewCart() {
        showInfo("Giỏ hàng - Chức năng đang phát triển");
    }

    @FXML
    private void handleViewOrders() {
        showInfo("Đơn hàng của tôi - Chức năng đang phát triển");
    }

    @FXML
    private void handleProfile() {
        showInfo("Thông tin cá nhân - Chức năng đang phát triển");
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

