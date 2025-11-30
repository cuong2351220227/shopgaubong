package com.example.shopgaubong.util;

import com.example.shopgaubong.entity.*;
import com.example.shopgaubong.enums.Role;
import com.example.shopgaubong.service.AuthService;
import com.example.shopgaubong.service.WarehouseService;
import com.example.shopgaubong.service.ItemService;
import com.example.shopgaubong.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    /**
     * Khởi tạo dữ liệu mẫu cho ứng dụng
     */
    public static void initializeSampleData() {
        logger.info("Bắt đầu khởi tạo dữ liệu mẫu...");

        try {
            createDefaultAccounts();
            createDefaultWarehouse();
            createDefaultStockItems();
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

    /**
     * Tạo kho mặc định
     */
    private static void createDefaultWarehouse() {
        WarehouseService warehouseService = new WarehouseService();

        try {
            // Kiểm tra nếu đã có kho
            List<Warehouse> warehouses = warehouseService.getAllWarehouses();
            if (warehouses.isEmpty()) {
                // Tạo kho mặc định với ID=1
                warehouseService.createWarehouse(
                    "KHO-001",
                    "Kho Trung Tâm",
                    "123 Đường ABC",
                    "TP. Hồ Chí Minh",
                    "Quận 1",
                    "Phường Bến Nghé",
                    "0901234567"
                );
                logger.info("Tạo kho mặc định: KHO-001 - Kho Trung Tâm");
            }
        } catch (Exception e) {
            logger.error("Lỗi khi tạo kho mặc định", e);
        }
    }

    /**
     * Tạo dữ liệu tồn kho mặc định cho các sản phẩm
     */
    private static void createDefaultStockItems() {
        StockService stockService = new StockService();
        ItemService itemService = new ItemService();
        WarehouseService warehouseService = new WarehouseService();

        try {
            // Lấy kho đầu tiên (kho mặc định)
            List<Warehouse> warehouses = warehouseService.getAllWarehouses();
            if (warehouses.isEmpty()) {
                logger.warn("Không tìm thấy kho nào để tạo tồn kho");
                return;
            }

            Warehouse defaultWarehouse = warehouses.getFirst();
            logger.info("Sử dụng kho: {} (ID: {})", defaultWarehouse.getName(), defaultWarehouse.getId());

            // Lấy tất cả sản phẩm active
            List<Item> items = itemService.getActiveItems();
            if (items.isEmpty()) {
                logger.warn("Không có sản phẩm nào để tạo tồn kho");
                return;
            }

            int createdCount = 0;
            for (Item item : items) {
                try {
                    // Kiểm tra xem đã tồn tại stock item chưa
                    List<StockItem> existingStock = stockService.getStockItemsByItem(item.getId());
                    if (existingStock.isEmpty()) {
                        // Tạo stock item với số lượng mặc định
                        stockService.createStockItem(
                            defaultWarehouse.getId(),
                            item.getId(),
                            100, // Số lượng tồn kho ban đầu
                            10   // Ngưỡng cảnh báo tồn kho thấp
                        );
                        createdCount++;
                        logger.info("Tạo tồn kho cho sản phẩm: {} (100 đơn vị)", item.getName());
                    }
                } catch (IllegalArgumentException e) {
                    // Stock item đã tồn tại, bỏ qua
                    logger.debug("Tồn kho đã tồn tại cho sản phẩm: {}", item.getName());
                }
            }

            logger.info("Đã tạo {} mục tồn kho trong kho {}", createdCount, defaultWarehouse.getName());
        } catch (Exception e) {
            logger.error("Lỗi khi tạo dữ liệu tồn kho mặc định", e);
        }
    }
}

