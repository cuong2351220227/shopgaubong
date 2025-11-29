package com.example.shopgaubong.util;

import com.example.shopgaubong.entity.*;
import com.example.shopgaubong.enums.Role;
import com.example.shopgaubong.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    /**
     * Khởi tạo dữ liệu mẫu cho ứng dụng
     */
    public static void initializeSampleData() {
        logger.info("Bắt đầu khởi tạo dữ liệu mẫu...");

        try {
            createDefaultAccounts();
            logger.info("Hoàn tất khởi tạo dữ liệu mẫu");
        } catch (Exception e) {
            logger.error("Lỗi khi khởi tạo dữ liệu mẫu", e);
        }
    }

    /**
     * Tạo các tài khoản mặc định
     */
    private static void createDefaultAccounts() {
        AuthService authService = new AuthService();

        try {
            // Tạo tài khoản Admin
            if (authService.getAllAccounts().isEmpty()) {
                authService.register("admin", "admin123", "Quản Trị Viên", Role.ADMIN);
                logger.info("Tạo tài khoản Admin mặc định: admin/admin123");

                // Tạo tài khoản Staff
                authService.register("staff", "staff123", "Nhân Viên", Role.STAFF);
                logger.info("Tạo tài khoản Staff mặc định: staff/staff123");

                // Tạo tài khoản Customer
                authService.register("customer", "customer123", "Khách Hàng", Role.CUSTOMER);
                logger.info("Tạo tài khoản Customer mặc định: customer/customer123");
            }
        } catch (Exception e) {
            logger.error("Lỗi khi tạo tài khoản mặc định", e);
        }
    }
}

