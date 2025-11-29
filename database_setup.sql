-- ==========================================
-- UCOP - Shop Gấu Bông Database Setup
-- ==========================================

-- Tạo database
CREATE DATABASE IF NOT EXISTS shopgaubong
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Sử dụng database
USE shopgaubong;

-- Database sẽ được Hibernate tự động tạo bảng khi chạy ứng dụng lần đầu
-- với cấu hình hibernate.hbm2ddl.auto = update

-- Để kiểm tra các bảng đã được tạo:
-- SHOW TABLES;

-- Để xem cấu trúc các bảng:
-- DESCRIBE accounts;
-- DESCRIBE account_profiles;
-- DESCRIBE categories;
-- DESCRIBE items;
-- DESCRIBE warehouses;
-- DESCRIBE stock_items;
-- DESCRIBE orders;
-- DESCRIBE order_items;
-- DESCRIBE payments;
-- DESCRIBE shipments;
-- DESCRIBE promotions;

