package com.example.shopgaubong.controller;

import com.example.shopgaubong.entity.Account;
import com.example.shopgaubong.entity.AccountProfile;
import com.example.shopgaubong.enums.Role;
import com.example.shopgaubong.service.AccountManagementService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    // Table and columns
    @FXML private TableView<Account> accountTable;
    @FXML private TableColumn<Account, Long> colId;
    @FXML private TableColumn<Account, String> colUsername;
    @FXML private TableColumn<Account, String> colFullName;
    @FXML private TableColumn<Account, String> colEmail;
    @FXML private TableColumn<Account, String> colPhone;
    @FXML private TableColumn<Account, String> colRole;
    @FXML private TableColumn<Account, String> colStatus;
    @FXML private TableColumn<Account, String> colCreatedAt;

    // Search and filters
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterRole;
    @FXML private ComboBox<String> cmbFilterStatus;

    // Form fields
    @FXML private Label lblFormTitle;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Role> cmbRole;
    @FXML private CheckBox chkActive;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;

    // Form buttons
    @FXML private Button btnNew;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnResetPassword;

    // Account details (read-only)
    @FXML private VBox vboxAccountDetails;
    @FXML private Label lblAccountId;
    @FXML private Label lblCreatedDate;
    @FXML private Label lblUpdatedDate;

    // Stats labels
    @FXML private Label lblTotalAccounts;
    @FXML private Label lblActiveAccounts;
    @FXML private Label lblInactiveAccounts;

    private final AccountManagementService accountService = new AccountManagementService();
    private final ObservableList<Account> accountList = FXCollections.observableArrayList();
    private Account selectedAccount = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadAccounts();
        setupTableSelection();
        setupSearchFilter();
        setupRoleComboBox();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        
        colFullName.setCellValueFactory(cellData -> {
            AccountProfile profile = cellData.getValue().getProfile();
            return new SimpleStringProperty(profile != null ? profile.getFullName() : "");
        });
        
        colEmail.setCellValueFactory(cellData -> {
            AccountProfile profile = cellData.getValue().getProfile();
            return new SimpleStringProperty(profile != null ? profile.getEmail() : "");
        });
        
        colPhone.setCellValueFactory(cellData -> {
            AccountProfile profile = cellData.getValue().getProfile();
            return new SimpleStringProperty(profile != null ? profile.getPhone() : "");
        });
        
        colRole.setCellValueFactory(cellData -> 
            new SimpleStringProperty(getRoleDisplayName(cellData.getValue().getRole())));
        
        colStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getIsActive() ? "Hoạt động" : "Ngừng"));
        
        colCreatedAt.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
            }
            return new SimpleStringProperty("");
        });

        // Format status column with colors
        colStatus.setCellFactory(col -> new TableCell<Account, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("Hoạt động".equals(status)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Format role column with colors
        colRole.setCellFactory(col -> new TableCell<Account, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role);
                    String color = switch (role) {
                        case "Quản trị viên" -> "#e74c3c";
                        case "Nhân viên" -> "#3498db";
                        case "Khách hàng" -> "#27ae60";
                        default -> "#555";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void setupFilters() {
        // Role filter
        cmbFilterRole.setItems(FXCollections.observableArrayList(
            "Tất cả", "Quản trị viên", "Nhân viên", "Khách hàng"
        ));
        cmbFilterRole.setValue("Tất cả");
        cmbFilterRole.setOnAction(e -> applyFilters());

        // Status filter
        cmbFilterStatus.setItems(FXCollections.observableArrayList(
            "Tất cả", "Hoạt động", "Ngừng hoạt động"
        ));
        cmbFilterStatus.setValue("Tất cả");
        cmbFilterStatus.setOnAction(e -> applyFilters());
    }

    private void setupRoleComboBox() {
        cmbRole.setItems(FXCollections.observableArrayList(Role.values()));
        cmbRole.setCellFactory(lv -> new ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : getRoleDisplayName(item));
            }
        });
        cmbRole.setButtonCell(new ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : getRoleDisplayName(item));
            }
        });
    }

    private void loadAccounts() {
        try {
            List<Account> accounts = accountService.getAllAccounts();
            accountList.setAll(accounts);
            accountTable.setItems(accountList);
            updateStats();
            logger.info("Loaded {} accounts", accounts.size());
        } catch (Exception e) {
            logger.error("Error loading accounts: {}", e.getMessage(), e);
            showError("Lỗi khi tải danh sách tài khoản: " + e.getMessage());
        }
    }

    private void setupTableSelection() {
        accountTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showAccountDetails(newSelection);
                }
            });
    }

    private void setupSearchFilter() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        String searchText = txtSearch.getText().toLowerCase();
        String roleFilter = cmbFilterRole.getValue();
        String statusFilter = cmbFilterStatus.getValue();

        List<Account> filtered = accountService.getAllAccounts().stream()
            .filter(account -> {
                // Search filter
                boolean matchesSearch = searchText.isEmpty() ||
                    account.getUsername().toLowerCase().contains(searchText) ||
                    (account.getProfile() != null && 
                     (account.getProfile().getFullName().toLowerCase().contains(searchText) ||
                      (account.getProfile().getEmail() != null && 
                       account.getProfile().getEmail().toLowerCase().contains(searchText))));

                // Role filter
                boolean matchesRole = "Tất cả".equals(roleFilter) ||
                    getRoleDisplayName(account.getRole()).equals(roleFilter);

                // Status filter
                boolean matchesStatus = "Tất cả".equals(statusFilter) ||
                    ("Hoạt động".equals(statusFilter) && account.getIsActive()) ||
                    ("Ngừng hoạt động".equals(statusFilter) && !account.getIsActive());

                return matchesSearch && matchesRole && matchesStatus;
            })
            .collect(Collectors.toList());

        accountList.setAll(filtered);
        updateStats();
    }

    private void updateStats() {
        int total = accountList.size();
        long active = accountList.stream().filter(Account::getIsActive).count();
        long inactive = total - active;

        lblTotalAccounts.setText("Tổng: " + total + " tài khoản");
        lblActiveAccounts.setText("Đang hoạt động: " + active);
        lblInactiveAccounts.setText("Ngừng hoạt động: " + inactive);
    }

    private void showAccountDetails(Account account) {
        selectedAccount = account;
        lblFormTitle.setText("✏️ Chỉnh sửa tài khoản");

        txtUsername.setText(account.getUsername());
        txtPassword.clear();
        txtPassword.setPromptText("Để trống nếu không đổi mật khẩu");
        cmbRole.setValue(account.getRole());
        chkActive.setSelected(account.getIsActive());

        AccountProfile profile = account.getProfile();
        if (profile != null) {
            txtFullName.setText(profile.getFullName());
            txtEmail.setText(profile.getEmail() != null ? profile.getEmail() : "");
            txtPhone.setText(profile.getPhone() != null ? profile.getPhone() : "");
        }

        // Show account details
        vboxAccountDetails.setVisible(true);
        vboxAccountDetails.setManaged(true);
        lblAccountId.setText(String.valueOf(account.getId()));
        
        if (account.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            lblCreatedDate.setText(account.getCreatedAt().format(formatter));
        }
        
        if (account.getUpdatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            lblUpdatedDate.setText(account.getUpdatedAt().format(formatter));
        }

        // Show delete and reset password buttons
        btnDelete.setVisible(true);
        btnDelete.setManaged(true);
        btnResetPassword.setVisible(true);
        btnResetPassword.setManaged(true);
    }

    @FXML
    private void handleNew() {
        clearForm();
        selectedAccount = null;
        lblFormTitle.setText("📝 Thêm tài khoản mới");
        txtUsername.requestFocus();
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText();
            Role role = cmbRole.getValue();
            String fullName = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            Boolean isActive = chkActive.isSelected();

            if (selectedAccount == null) {
                // Create new account
                if (password.isEmpty()) {
                    showWarning("Vui lòng nhập mật khẩu!");
                    txtPassword.requestFocus();
                    return;
                }

                accountService.createAccount(username, password, role, fullName,
                    email.isEmpty() ? null : email,
                    phone.isEmpty() ? null : phone);
                showSuccess("Thêm tài khoản thành công!");
            } else {
                // Update existing account
                accountService.updateAccount(selectedAccount.getId(), username, role, fullName,
                    email.isEmpty() ? null : email,
                    phone.isEmpty() ? null : phone, isActive);
                showSuccess("Cập nhật tài khoản thành công!");
            }

            loadAccounts();
            clearForm();
            selectedAccount = null;

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            logger.error("Error saving account: {}", e.getMessage(), e);
            showError("Lỗi khi lưu tài khoản: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedAccount == null) {
            showWarning("Vui lòng chọn tài khoản cần xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa tài khoản: " + selectedAccount.getUsername());
        confirm.setContentText("Bạn muốn vô hiệu hóa hay xóa vĩnh viễn tài khoản này?");

        ButtonType btnDeactivate = new ButtonType("🔒 Vô hiệu hóa");
        ButtonType btnDeletePermanent = new ButtonType("🗑️ Xóa vĩnh viễn");
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirm.getButtonTypes().setAll(btnDeactivate, btnDeletePermanent, btnCancel);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent()) {
            try {
                if (result.get() == btnDeactivate) {
                    accountService.deactivateAccount(selectedAccount.getId());
                    showSuccess("Đã vô hiệu hóa tài khoản!");
                } else if (result.get() == btnDeletePermanent) {
                    accountService.deleteAccount(selectedAccount.getId());
                    showSuccess("Đã xóa tài khoản vĩnh viễn!");
                }
                loadAccounts();
                clearForm();
                selectedAccount = null;
            } catch (Exception e) {
                logger.error("Error deleting account: {}", e.getMessage(), e);
                showError("Lỗi khi xóa tài khoản: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleResetPassword() {
        if (selectedAccount == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đặt lại mật khẩu");
        dialog.setHeaderText("Đặt lại mật khẩu cho: " + selectedAccount.getUsername());
        dialog.setContentText("Mật khẩu mới (tối thiểu 6 ký tự):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            if (newPassword.length() < 6) {
                showWarning("Mật khẩu phải có ít nhất 6 ký tự!");
                return;
            }

            try {
                accountService.resetPassword(selectedAccount.getId(), newPassword);
                showSuccess("Đã đặt lại mật khẩu thành công!");
            } catch (Exception e) {
                logger.error("Error resetting password: {}", e.getMessage(), e);
                showError("Lỗi khi đặt lại mật khẩu: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleCancel() {
        clearForm();
        selectedAccount = null;
        accountTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleRefresh() {
        loadAccounts();
        clearForm();
        selectedAccount = null;
        txtSearch.clear();
        cmbFilterRole.setValue("Tất cả");
        cmbFilterStatus.setValue("Tất cả");
    }

    private boolean validateInput() {
        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (username.isEmpty()) {
            showWarning("Vui lòng nhập tên đăng nhập!");
            txtUsername.requestFocus();
            return false;
        }

        if (!accountService.isValidUsername(username)) {
            showWarning("Tên đăng nhập không hợp lệ! Chỉ chữ, số và gạch dưới, 3-50 ký tự.");
            txtUsername.requestFocus();
            return false;
        }

        if (fullName.isEmpty()) {
            showWarning("Vui lòng nhập họ tên!");
            txtFullName.requestFocus();
            return false;
        }

        if (cmbRole.getValue() == null) {
            showWarning("Vui lòng chọn vai trò!");
            cmbRole.requestFocus();
            return false;
        }

        if (!email.isEmpty() && !accountService.isValidEmail(email)) {
            showWarning("Email không hợp lệ!");
            txtEmail.requestFocus();
            return false;
        }

        if (!phone.isEmpty() && !accountService.isValidPhone(phone)) {
            showWarning("Số điện thoại không hợp lệ! Phải bắt đầu bằng 0 và có 10-11 chữ số.");
            txtPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void clearForm() {
        lblFormTitle.setText("📝 Thêm tài khoản mới");
        txtUsername.clear();
        txtPassword.clear();
        txtPassword.setPromptText("Tối thiểu 6 ký tự");
        cmbRole.setValue(null);
        chkActive.setSelected(true);
        txtFullName.clear();
        txtEmail.clear();
        txtPhone.clear();

        vboxAccountDetails.setVisible(false);
        vboxAccountDetails.setManaged(false);
        btnDelete.setVisible(false);
        btnDelete.setManaged(false);
        btnResetPassword.setVisible(false);
        btnResetPassword.setManaged(false);
    }

    private String getRoleDisplayName(Role role) {
        return switch (role) {
            case ADMIN -> "Quản trị viên";
            case STAFF -> "Nhân viên";
            case CUSTOMER -> "Khách hàng";
        };
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
