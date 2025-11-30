package com.example.shopgaubong.controller;

import com.example.shopgaubong.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
        loadView("/com/example/shopgaubong/product-catalog-view.fxml", "Danh mục sản phẩm");
    }

    @FXML
    private void handleViewCart() {
        loadView("/com/example/shopgaubong/cart-view.fxml", "Giỏ hàng");
    }

    @FXML
    private void handleViewOrders() {
        // Load customer order view
        loadView("/com/example/shopgaubong/customer-order-view.fxml", "Đơn hàng của tôi");
    }

    @FXML
    private void handleProfile() {
        loadView("/com/example/shopgaubong/customer-profile-view.fxml", "Thông tin cá nhân");
    }

    /**
     * Load a view into the center content pane
     */
    private void loadView(String fxmlPath, String title) {
        try {
            System.out.println("Loading view: " + fxmlPath);
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

