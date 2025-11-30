package com.example.shopgaubong.service;

import com.example.shopgaubong.dao.AccountDAO;
import com.example.shopgaubong.entity.Account;
import com.example.shopgaubong.entity.AccountProfile;
import jakarta.persistence.EntityManager;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);
    private final AccountDAO accountDAO = new AccountDAO();

    /**
     * Update account profile information
     */
    public void updateProfile(Long accountId, String fullName, String email, String phone, 
                             String address, String city, String district, String ward) {
        try {
            Optional<Account> accountOpt = accountDAO.findById(accountId);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("Tài khoản không tồn tại");
            }

            Account account = accountOpt.get();
            AccountProfile profile = account.getProfile();
            if (profile == null) {
                throw new IllegalStateException("Profile không tồn tại");
            }

            // Validate email uniqueness if changed
            if (email != null && !email.equals(profile.getEmail())) {
                if (isEmailExists(email, accountId)) {
                    throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác");
                }
            }

            // Update profile fields
            if (fullName != null && !fullName.trim().isEmpty()) {
                profile.setFullName(fullName.trim());
            }
            
            if (email != null && !email.trim().isEmpty()) {
                profile.setEmail(email.trim());
            }
            
            if (phone != null && !phone.trim().isEmpty()) {
                profile.setPhone(phone.trim());
            }
            
            profile.setAddress(address != null ? address.trim() : null);
            profile.setCity(city != null ? city.trim() : null);
            profile.setDistrict(district != null ? district.trim() : null);
            profile.setWard(ward != null ? ward.trim() : null);

            // Update account (cascade will update profile)
            accountDAO.update(account);
            
            logger.info("Updated profile for account ID: {}", accountId);
        } catch (Exception e) {
            logger.error("Error updating profile: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi cập nhật thông tin: " + e.getMessage(), e);
        }
    }

    /**
     * Change account password
     */
    public void changePassword(Long accountId, String currentPassword, String newPassword) {
        try {
            Optional<Account> accountOpt = accountDAO.findById(accountId);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("Tài khoản không tồn tại");
            }

            Account account = accountOpt.get();

            // Verify current password
            if (!BCrypt.checkpw(currentPassword, account.getPassword())) {
                throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
            }

            // Validate new password
            if (newPassword == null || newPassword.length() < 6) {
                throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");
            }

            // Hash and update password
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            account.setPassword(hashedPassword);
            accountDAO.update(account);

            logger.info("Changed password for account ID: {}", accountId);
        } catch (Exception e) {
            logger.error("Error changing password: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi đổi mật khẩu: " + e.getMessage(), e);
        }
    }

    /**
     * Get account profile by account ID
     */
    public AccountProfile getProfileByAccountId(Long accountId) {
        try {
            Optional<Account> accountOpt = accountDAO.findById(accountId);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("Tài khoản không tồn tại");
            }
            return accountOpt.get().getProfile();
        } catch (Exception e) {
            logger.error("Error getting profile: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy thông tin: " + e.getMessage(), e);
        }
    }

    /**
     * Check if email already exists for another account
     */
    private boolean isEmailExists(String email, Long excludeAccountId) {
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
     * Validate phone number format (Vietnamese format)
     */
    public boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Allow empty
        }
        return phone.matches("^0[0-9]{9,10}$");
    }

    /**
     * Validate email format
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Allow empty
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
