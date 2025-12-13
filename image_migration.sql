-- Migration: Thay đổi cột image_url thành image_data để lưu ảnh Base64
-- Ngày: 13/12/2025

USE shopgaubong;

-- Bước 1: Thêm cột mới image_data (LONGTEXT để lưu Base64)
ALTER TABLE items 
ADD COLUMN image_data LONGTEXT AFTER image_url;

-- Bước 2: Copy dữ liệu từ image_url sang image_data (nếu có)
-- Lưu ý: Dữ liệu cũ là URL, sau này sẽ là Base64
UPDATE items 
SET image_data = image_url 
WHERE image_url IS NOT NULL AND image_url != '';

-- Bước 3: Xóa cột cũ image_url
ALTER TABLE items 
DROP COLUMN image_url;

-- Kiểm tra cấu trúc mới
DESCRIBE items;

COMMIT;

-- Hướng dẫn sử dụng:
-- 1. Sau khi chạy migration này, ảnh sẽ được lưu dưới dạng Base64 trong cột image_data
-- 2. Kích thước LONGTEXT hỗ trợ lưu ảnh lên đến ~4GB (đủ cho hầu hết ảnh sản phẩm)
-- 3. Nên resize ảnh trước khi lưu (max 800x800px) để tiết kiệm dung lượng
