package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.AccountDAO;
import com.example.shopgaubong.entity.Account;
import com.example.shopgaubong.entity.AccountProfile;
import com.example.shopgaubong.enums.Role;
import com.example.shopgaubong.util.PasswordUtil;
import com.example.shopgaubong.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final AccountDAO accountDAO;
    private final SessionManager sessionManager;

    public AuthService() {
        this.accountDAO = new AccountDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Đăng nhập
     */
    public boolean login(String username, String password) {
        try {
            Optional<Account> accountOpt = accountDAO.findByUsername(username);

            if (accountOpt.isEmpty()) {
                logger.warn("Đăng nhập thất bại: Tài khoản {} không tồn tại", username);
                return false;
            }

            Account account = accountOpt.get();

            if (!account.getIsActive()) {
                logger.warn("Đăng nhập thất bại: Tài khoản {} đã bị khóa", username);
                return false;
            }

            if (!PasswordUtil.checkPassword(password, account.getPassword())) {
                logger.warn("Đăng nhập thất bại: Sai mật khẩu cho tài khoản {}", username);
                return false;
            }

            sessionManager.login(account);
            logger.info("Đăng nhập thành công: {}", username);
            return true;

        } catch (Exception e) {
            logger.error("Lỗi khi đăng nhập", e);
            return false;
        }
    }

    /**
     * Đăng xuất
     */
    public void logout() {
        String username = sessionManager.getCurrentUsername();
        sessionManager.logout();
        logger.info("Đăng xuất: {}", username);
    }

    /**
     * Đăng ký tài khoản mới
     */
    public Account register(String username, String password, String fullName, Role role) {
        if (accountDAO.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(PasswordUtil.hashPassword(password));
        account.setRole(role);
        account.setIsActive(true);

        AccountProfile profile = new AccountProfile();
        profile.setFullName(fullName);
        account.setProfile(profile);

        account.setCreatedBy("SYSTEM");
        account.setUpdatedBy("SYSTEM");
        profile.setCreatedBy("SYSTEM");
        profile.setUpdatedBy("SYSTEM");

        Account savedAccount = accountDAO.save(account);
        logger.info("Đăng ký tài khoản mới: {}", username);
        return savedAccount;
    }

    /**
     * Đổi mật khẩu
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        try {
            Optional<Account> accountOpt = accountDAO.findByUsername(username);

            if (accountOpt.isEmpty()) {
                return false;
            }

            Account account = accountOpt.get();

            if (!PasswordUtil.checkPassword(oldPassword, account.getPassword())) {
                logger.warn("Đổi mật khẩu thất bại: Sai mật khẩu cũ cho tài khoản {}", username);
                return false;
            }

            account.setPassword(PasswordUtil.hashPassword(newPassword));
            account.setUpdatedBy(username);
            accountDAO.update(account);

            logger.info("Đổi mật khẩu thành công: {}", username);
            return true;

        } catch (Exception e) {
            logger.error("Lỗi khi đổi mật khẩu", e);
            return false;
        }
    }

    /**
     * Khóa/Mở khóa tài khoản
     */
    public void toggleAccountStatus(Long accountId, boolean isActive) {
        accountDAO.updateAccountStatus(accountId, isActive);
        logger.info("Cập nhật trạng thái tài khoản ID {}: {}", accountId, isActive ? "Mở khóa" : "Khóa");
    }

    /**
     * Lấy tài khoản hiện tại
     */
    public Account getCurrentAccount() {
        return sessionManager.getCurrentAccount();
    }

    /**
     * Kiểm tra đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    /**
     * Lấy tất cả tài khoản
     */
    public List<Account> getAllAccounts() {
        return accountDAO.findAll();
    }

    /**
     * Lấy tài khoản theo ID
     */
    public Optional<Account> getAccountById(Long id) {
        return accountDAO.findById(id);
    }
}

