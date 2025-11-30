package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Account;
import com.example.shopgaubong.entity.AccountProfile;
import com.example.shopgaubong.service.AuthService;
import com.example.shopgaubong.service.ProfileService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

public class CustomerProfileController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerProfileController.class);

    // Profile Information Fields
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtAddress;
    @FXML private TextField txtCity;
    @FXML private TextField txtDistrict;
    @FXML private TextField txtWard;

    // Password Fields
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    // Account Info Labels
    @FXML private Label lblUsername;
    @FXML private Label lblRole;
    @FXML private Label lblCreatedAt;
    @FXML private Label lblStatus;

    // Top Save Button
    @FXML private Button btnSaveAll;

    private final AuthService authService = new AuthService();
    private final ProfileService profileService = new ProfileService();
    private Account currentAccount;
    private AccountProfile originalProfile;

    @FXML
    public void initialize() {
        loadCurrentAccount();
        loadProfileData();
    }

    /**
     * Load current logged-in account
     */
    private void loadCurrentAccount() {
        try {
            currentAccount = authService.getCurrentAccount();
            if (currentAccount == null) {
                showError("Không tìm thấy thông tin tài khoản. Vui lòng đăng nhập lại.");
                return;
            }
            
            // Display account information
            lblUsername.setText(currentAccount.getUsername());
            lblRole.setText(getRoleDisplayName(currentAccount.getRole().name()));
            
            // Display status with color
            boolean isActive = currentAccount.getIsActive() != null && currentAccount.getIsActive();
            lblStatus.setText(isActive ? "✓ Đang hoạt động" : "✗ Ngừng hoạt động");
            lblStatus.setStyle(isActive ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;" : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            
            if (currentAccount.getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                lblCreatedAt.setText(currentAccount.getCreatedAt().format(formatter));
            }
            
            logger.info("Loaded account information for user: {}", currentAccount.getUsername());
        } catch (Exception e) {
            logger.error("Error loading current account: {}", e.getMessage(), e);
            showError("Lỗi khi tải thông tin tài khoản: " + e.getMessage());
        }
    }

    /**
     * Load profile data into form fields
     */
    private void loadProfileData() {
        try {
            if (currentAccount == null || currentAccount.getProfile() == null) {
                showError("Không tìm thấy thông tin cá nhân");
                return;
            }

            originalProfile = currentAccount.getProfile();
            
            txtFullName.setText(originalProfile.getFullName() != null ? originalProfile.getFullName() : "");
            txtEmail.setText(originalProfile.getEmail() != null ? originalProfile.getEmail() : "");
            txtPhone.setText(originalProfile.getPhone() != null ? originalProfile.getPhone() : "");
            txtAddress.setText(originalProfile.getAddress() != null ? originalProfile.getAddress() : "");
            txtCity.setText(originalProfile.getCity() != null ? originalProfile.getCity() : "");
            txtDistrict.setText(originalProfile.getDistrict() != null ? originalProfile.getDistrict() : "");
            txtWard.setText(originalProfile.getWard() != null ? originalProfile.getWard() : "");
            
            logger.info("Loaded profile data for user: {}", currentAccount.getUsername());
        } catch (Exception e) {
            logger.error("Error loading profile data: {}", e.getMessage(), e);
            showError("Lỗi khi tải thông tin cá nhân: " + e.getMessage());
        }
    }

    /**
     * Handle update profile button click
     */
    @FXML
    private void handleUpdateProfile() {
        if (!validateProfileInput()) {
            return;
        }

        try {
            String fullName = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            String address = txtAddress.getText().trim();
            String city = txtCity.getText().trim();
            String district = txtDistrict.getText().trim();
            String ward = txtWard.getText().trim();

            profileService.updateProfile(
                currentAccount.getId(),
                fullName,
                email.isEmpty() ? null : email,
                phone.isEmpty() ? null : phone,
                address.isEmpty() ? null : address,
                city.isEmpty() ? null : city,
                district.isEmpty() ? null : district,
                ward.isEmpty() ? null : ward
            );

            showSuccess("Cập nhật thông tin cá nhân thành công!");
            
            // Reload account data
            loadCurrentAccount();
            loadProfileData();
            
            logger.info("Profile updated successfully for user: {}", currentAccount.getUsername());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating profile: {}", e.getMessage(), e);
            showError("Lỗi khi cập nhật thông tin: " + e.getMessage());
        }
    }

    /**
     * Handle cancel profile update - refresh to original data
     */
    @FXML
    private void handleRefreshProfile() {
        loadProfileData();
        showInfo("Đã làm mới thông tin");
    }

    /**
     * Handle save all button - saves profile information
     */
    @FXML
    private void handleSaveAll() {
        handleUpdateProfile();
    }

    /**
     * Handle change password button click
     */
    @FXML
    private void handleChangePassword() {
        if (!validatePasswordInput()) {
            return;
        }

        try {
            String currentPassword = txtCurrentPassword.getText();
            String newPassword = txtNewPassword.getText();
            String confirmPassword = txtConfirmPassword.getText();

            // Check if new password matches confirmation
            if (!newPassword.equals(confirmPassword)) {
                showWarning("Mật khẩu mới và xác nhận mật khẩu không khớp!");
                txtConfirmPassword.requestFocus();
                return;
            }

            // Check if new password is different from current
            if (currentPassword.equals(newPassword)) {
                showWarning("Mật khẩu mới phải khác mật khẩu hiện tại!");
                txtNewPassword.requestFocus();
                return;
            }

            profileService.changePassword(currentAccount.getId(), currentPassword, newPassword);

            showSuccess("Đổi mật khẩu thành công!");
            clearPasswordFields();
            
            logger.info("Password changed successfully for user: {}", currentAccount.getUsername());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            logger.error("Error changing password: {}", e.getMessage(), e);
            showError("Lỗi khi đổi mật khẩu: " + e.getMessage());
        }
    }

    /**
     * Handle cancel password change
     */
    @FXML
    private void handleCancelPassword() {
        clearPasswordFields();
        showInfo("Đã hủy đổi mật khẩu");
    }

    /**
     * Validate profile input fields
     */
    private boolean validateProfileInput() {
        if (txtFullName.getText().trim().isEmpty()) {
            showWarning("Vui lòng nhập họ tên!");
            txtFullName.requestFocus();
            return false;
        }

        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !profileService.isValidEmail(email)) {
            showWarning("Email không hợp lệ!");
            txtEmail.requestFocus();
            return false;
        }

        String phone = txtPhone.getText().trim();
        if (!phone.isEmpty() && !profileService.isValidPhoneNumber(phone)) {
            showWarning("Số điện thoại không hợp lệ! Phải bắt đầu bằng 0 và có 10-11 chữ số.");
            txtPhone.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Validate password input fields
     */
    private boolean validatePasswordInput() {
        if (txtCurrentPassword.getText().isEmpty()) {
            showWarning("Vui lòng nhập mật khẩu hiện tại!");
            txtCurrentPassword.requestFocus();
            return false;
        }

        if (txtNewPassword.getText().isEmpty()) {
            showWarning("Vui lòng nhập mật khẩu mới!");
            txtNewPassword.requestFocus();
            return false;
        }

        if (txtNewPassword.getText().length() < 6) {
            showWarning("Mật khẩu mới phải có ít nhất 6 ký tự!");
            txtNewPassword.requestFocus();
            return false;
        }

        if (txtConfirmPassword.getText().isEmpty()) {
            showWarning("Vui lòng xác nhận mật khẩu mới!");
            txtConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Clear all password fields
     */
    private void clearPasswordFields() {
        txtCurrentPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();
    }

    /**
     * Get display name for role
     */
    private String getRoleDisplayName(String role) {
        return switch (role) {
            case "ADMIN" -> "Quản trị viên";
            case "STAFF" -> "Nhân viên";
            case "CUSTOMER" -> "Khách hàng";
            default -> role;
        };
    }

    // Alert Methods
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
