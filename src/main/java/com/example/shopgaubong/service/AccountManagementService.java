package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.AccountDAO;
import com.example.shopgaubong.entity.Account;
import com.example.shopgaubong.entity.AccountProfile;
import com.example.shopgaubong.enums.Role;
import jakarta.persistence.EntityManager;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class AccountManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AccountManagementService.class);
    private final AccountDAO accountDAO = new AccountDAO();

    /**
     * Lấy tất cả tài khoản trong hệ thống
     * @return Danh sách tất cả tài khoản
     * @throws RuntimeException nếu có lỗi khi truy vấn database
     */
    public List<Account> getAllAccounts() {
        try {
            return accountDAO.findAll();
        } catch (Exception e) {
            logger.error("Error getting all accounts: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy danh sách tài khoản: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách tài khoản theo vai trò
     * @param role Vai trò cần lọc (ADMIN, STAFF, CUSTOMER)
     * @return Danh sách tài khoản có vai trò tương ứng
     * @throws RuntimeException nếu có lỗi khi truy vấn database
     */
    public List<Account> getAccountsByRole(Role role) {
        try {
            return accountDAO.findByRole(role);
        } catch (Exception e) {
            logger.error("Error getting accounts by role: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy danh sách tài khoản theo vai trò: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách tài khoản đang hoạt động
     * @return Danh sách tài khoản có trạng thái isActive = true
     * @throws RuntimeException nếu có lỗi khi truy vấn database
     */
    public List<Account> getActiveAccounts() {
        try {
            return accountDAO.findActiveAccounts();
        } catch (Exception e) {
            logger.error("Error getting active accounts: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy danh sách tài khoản hoạt động: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo tài khoản mới kèm thông tin profile
     * @param username Tên đăng nhập (phải duy nhất)
     * @param password Mật khẩu (sẽ được mã hóa bằng BCrypt)
     * @param role Vai trò của tài khoản
     * @param fullName Họ tên đầy đủ
     * @param email Email (có thể null, phải duy nhất nếu cung cấp)
     * @param phone Số điện thoại (có thể null)
     * @return Tài khoản vừa được tạo
     * @throws IllegalArgumentException nếu username hoặc email đã tồn tại
     * @throws RuntimeException nếu có lỗi khi lưu database
     */
    public Account createAccount(String username, String password, Role role, String fullName,
                                 String email, String phone) {
        try {
            // Validate username uniqueness
            if (accountDAO.existsByUsername(username)) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
            }

            // Validate email uniqueness if provided
            if (email != null && !email.trim().isEmpty()) {
                if (isEmailExists(email)) {
                    throw new IllegalArgumentException("Email đã được sử dụng");
                }
            }

            // Create account
            Account account = new Account();
            account.setUsername(username);
            account.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            account.setRole(role);
            account.setIsActive(true);

            // Create profile
            AccountProfile profile = new AccountProfile();
            profile.setFullName(fullName);
            profile.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);
            profile.setPhone(phone != null && !phone.trim().isEmpty() ? phone.trim() : null);
            profile.setAccount(account);

            account.setProfile(profile);

            // Save account (cascade will save profile)
            Account savedAccount = accountDAO.save(account);
            logger.info("Created new account: {}", username);
            
            return savedAccount;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating account: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tạo tài khoản: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin tài khoản
     * @param accountId ID của tài khoản cần cập nhật
     * @param username Tên đăng nhập mới (phải duy nhất nếu thay đổi)
     * @param role Vai trò mới
     * @param fullName Họ tên mới
     * @param email Email mới (phải duy nhất nếu thay đổi)
     * @param phone Số điện thoại mới
     * @param isActive Trạng thái hoạt động
     * @throws IllegalArgumentException nếu tài khoản không tồn tại hoặc username/email đã được sử dụng
     * @throws RuntimeException nếu có lỗi khi cập nhật database
     */
    public void updateAccount(Long accountId, String username, Role role, String fullName,
                             String email, String phone, Boolean isActive) {
        try {
            Optional<Account> accountOpt = accountDAO.findById(accountId);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("Tài khoản không tồn tại");
            }

            Account account = accountOpt.get();

            // Check if username changed and validate uniqueness
            if (!account.getUsername().equals(username)) {
                if (accountDAO.existsByUsername(username)) {
                    throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
                }
                account.setUsername(username);
            }

            // Update account fields
            account.setRole(role);
            account.setIsActive(isActive);

            // Update profile
            AccountProfile profile = account.getProfile();
            if (profile != null) {
                // Validate email uniqueness if changed
                if (email != null && !email.equals(profile.getEmail())) {
                    if (isEmailExistsExcluding(email, accountId)) {
                        throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác");
                    }
                }

                profile.setFullName(fullName);
                profile.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);
                profile.setPhone(phone != null && !phone.trim().isEmpty() ? phone.trim() : null);
            }

            accountDAO.update(account);
            logger.info("Updated account: {}", username);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating account: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi cập nhật tài khoản: " + e.getMessage(), e);
        }
    }

    /**
     * Đặt lại mật khẩu cho tài khoản
     * @param accountId ID của tài khoản cần đặt lại mật khẩu
     * @param newPassword Mật khẩu mới (sẽ được mã hóa bằng BCrypt)
     * @throws IllegalArgumentException nếu tài khoản không tồn tại
     * @throws RuntimeException nếu có lỗi khi cập nhật database
     */
    public void resetPassword(Long accountId, String newPassword) {
        try {
            Optional<Account> accountOpt = accountDAO.findById(accountId);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("Tài khoản không tồn tại");
            }

            Account account = accountOpt.get();
            account.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            accountDAO.update(account);
            
            logger.info("Reset password for account ID: {}", accountId);
        } catch (Exception e) {
            logger.error("Error resetting password: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi đặt lại mật khẩu: " + e.getMessage(), e);
        }
    }

    /**
     * Vô hiệu hóa tài khoản (soft delete)
     * Đặt trạng thái isActive = false, không xóa khỏi database
     * @param accountId ID của tài khoản cần vô hiệu hóa
     * @throws RuntimeException nếu có lỗi khi cập nhật database
     */
    public void deactivateAccount(Long accountId) {
        try {
            accountDAO.updateAccountStatus(accountId, false);
            logger.info("Deactivated account ID: {}", accountId);
        } catch (Exception e) {
            logger.error("Error deactivating account: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi vô hiệu hóa tài khoản: " + e.getMessage(), e);
        }
    }

    /**
     * Kích hoạt lại tài khoản
     * Đặt trạng thái isActive = true
     * @param accountId ID của tài khoản cần kích hoạt
     * @throws RuntimeException nếu có lỗi khi cập nhật database
     */
    public void activateAccount(Long accountId) {
        try {
            accountDAO.updateAccountStatus(accountId, true);
            logger.info("Activated account ID: {}", accountId);
        } catch (Exception e) {
            logger.error("Error activating account: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi kích hoạt tài khoản: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa vĩnh viễn tài khoản khỏi database (hard delete)
     * Cảnh báo: Thao tác này không thể hoàn tác
     * @param accountId ID của tài khoản cần xóa
     * @throws RuntimeException nếu có lỗi khi xóa database
     */
    public void deleteAccount(Long accountId) {
        try {
            accountDAO.deleteById(accountId);
            logger.info("Permanently deleted account ID: {}", accountId);
        } catch (Exception e) {
            logger.error("Error deleting account: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xóa tài khoản: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra email đã tồn tại trong hệ thống chưa
     * @param email Email cần kiểm tra
     * @return true nếu email đã được sử dụng, false nếu chưa
     */
    private boolean isEmailExists(String email) {
        EntityManager em = accountDAO.getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(ap) FROM AccountProfile ap WHERE ap.email = :email",
                Long.class)
                .setParameter("email", email)
                .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Error checking email existence: {}", e.getMessage(), e);
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra email đã tồn tại ở tài khoản khác chưa
     * Dùng khi cập nhật tài khoản để loại trừ email của chính tài khoản đó
     * @param email Email cần kiểm tra
     * @param excludeAccountId ID tài khoản được loại trừ khỏi kiểm tra
     * @return true nếu email đã được sử dụng bởi tài khoản khác
     */
    private boolean isEmailExistsExcluding(String email, Long excludeAccountId) {
        EntityManager em = accountDAO.getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(ap) FROM AccountProfile ap WHERE ap.email = :email AND ap.account.id != :accountId",
                Long.class)
                .setParameter("email", email)
                .setParameter("accountId", excludeAccountId)
                .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Error checking email existence: {}", e.getMessage(), e);
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra định dạng tên đăng nhập hợp lệ
     * Yêu cầu: 3-50 ký tự, chỉ chứa chữ, số và gạch dưới
     * @param username Tên đăng nhập cần kiểm tra
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    public boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        // Username should be 3-50 characters, alphanumeric and underscore
        return username.matches("^[a-zA-Z0-9_]{3,50}$");
    }

    /**
     * Kiểm tra độ mạnh mật khẩu
     * Yêu cầu: Tối thiểu 6 ký tự
     * @param password Mật khẩu cần kiểm tra
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Kiểm tra định dạng email hợp lệ
     * Cho phép để trống, nếu có thì phải đúng format email
     * @param email Email cần kiểm tra
     * @return true nếu hợp lệ hoặc rỗng, false nếu không đúng định dạng
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Allow empty
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Kiểm tra định dạng số điện thoại Việt Nam
     * Cho phép để trống, nếu có thì phải bắt đầu bằng 0 và có 10-11 chữ số
     * @param phone Số điện thoại cần kiểm tra
     * @return true nếu hợp lệ hoặc rỗng, false nếu không đúng định dạng
     */
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Allow empty
        }
        return phone.matches("^0[0-9]{9,10}$");
    }
}
