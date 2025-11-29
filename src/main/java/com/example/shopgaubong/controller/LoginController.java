package com.example.shopgaubong.controller;

import com.example.shopgaubong.enums.Role;
import com.example.shopgaubong.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        messageLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập và mật khẩu!");
            return;
        }

        boolean success = authService.login(username, password);

        if (success) {
            Role role = authService.getCurrentAccount().getRole();
            showSuccess("Đăng nhập thành công!");

            // Chuyển sang màn hình chính tùy theo vai trò
            navigateToMainScreen(role);
        } else {
            showError("Tên đăng nhập hoặc mật khẩu không đúng!");
        }
    }

    private void navigateToMainScreen(Role role) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader;

            switch (role) {
                case ADMIN:
                    loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/admin-main.fxml"));
                    break;
                case STAFF:
                    loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/staff-main.fxml"));
                    break;
                case CUSTOMER:
                    loader = new FXMLLoader(getClass().getResource("/com/example/shopgaubong/customer-main.fxml"));
                    break;
                default:
                    showError("Vai trò không hợp lệ!");
                    return;
            }

            Scene scene = new Scene(loader.load(), 1200, 700);
            stage.setScene(scene);
            stage.setTitle("UCOP - Shop Gấu Bông");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi chuyển màn hình: " + e.getMessage());
        }
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText(message);
    }
}

