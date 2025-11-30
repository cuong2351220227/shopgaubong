-- =============================================
-- DỮ LIỆU MẪU ĐỂ TEST CHỨC NĂNG THANH TOÁN
-- Shop Gấu Bông
-- =============================================

USE shopgaubong;

-- =============================================
-- 1. TẠO TÀI KHOẢN TEST (nếu chưa có)
-- =============================================

-- Xóa dữ liệu test cũ (nếu có)
DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE order_number LIKE 'TEST%');
DELETE FROM refunds WHERE refund_number LIKE 'TEST%';
DELETE FROM payments WHERE transaction_id LIKE 'TEST%';
DELETE FROM orders WHERE order_number LIKE 'TEST%';

-- Tạo customer test (nếu chưa có)
INSERT IGNORE INTO accounts (username, password, email, role, is_active, created_by, updated_by, created_at, updated_at)
VALUES ('customer_test', '$2a$10$N9qo8uLOickgx2ZMRZoMye1234567890abcdefghij', 'customer@test.com', 'CUSTOMER', TRUE, 'system', 'system', NOW(), NOW());

-- Lấy ID của customer_test
SET @customer_id = (SELECT id FROM accounts WHERE username = 'customer_test');

-- Nếu không có customer_test, dùng customer ID đầu tiên
SET @customer_id = COALESCE(@customer_id, (SELECT id FROM accounts WHERE role = 'CUSTOMER' LIMIT 1));

-- Tạo profile cho customer (nếu chưa có)
INSERT IGNORE INTO account_profiles (account_id, full_name, phone, address, city, created_at, updated_at)
VALUES (@customer_id, 'Nguyễn Văn Test', '0901234567', '123 Test Street', 'TP.HCM', NOW(), NOW());

-- Hiển thị customer_id đang dùng
SELECT CONCAT('✓ Đang dùng Customer ID: ', @customer_id) as Info;

-- =============================================
-- 2. TẠO DANH MỤC VÀ SẢN PHẨM (nếu chưa có)
-- =============================================

-- Tạo category
INSERT IGNORE INTO categories (id, name, description, is_active, created_by, updated_by, created_at, updated_at)
VALUES (1, 'Gấu Bông', 'Các loại gấu bông dễ thương', TRUE, 'system', 'system', NOW(), NOW());

-- Tạo items test
INSERT IGNORE INTO items (id, name, description, price, category_id, is_active, created_by, updated_by, created_at, updated_at)
VALUES
(1, 'Gấu Brown Lớn', 'Gấu Brown size lớn 50cm', 250000, 1, TRUE, 'system', 'system', NOW(), NOW()),
(2, 'Gấu Teddy Trung', 'Gấu Teddy size trung 30cm', 150000, 1, TRUE, 'system', 'system', NOW(), NOW()),
(3, 'Gấu Panda Nhỏ', 'Gấu Panda size nhỏ 20cm', 100000, 1, TRUE, 'system', 'system', NOW(), NOW());

-- =============================================
-- 3. TẠO ĐỐN HÀNG TEST
-- =============================================

-- Đơn hàng 1: Đơn nhỏ - Test phí COD tối thiểu
INSERT INTO orders (
    order_number, customer_id, status,
    subtotal, discount, tax, shipping_fee, grand_total,
    shipping_address, shipping_city, shipping_district, shipping_ward,
    shipping_phone, shipping_receiver_name,
    notes, created_by, updated_by, created_at, updated_at
) VALUES (
    'TEST001', @customer_id, 'PENDING_PAYMENT',
    200000, 0, 0, 20000, 220000,
    '123 Đường ABC, Phường 1', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường 1',
    '0901234567', 'Nguyễn Văn Test',
    'Đơn nhỏ - Test phí COD tối thiểu 10,000đ',
    'system', 'system', NOW(), NOW()
);

SET @order1_id = LAST_INSERT_ID();

-- Items cho order 1
INSERT INTO order_items (order_id, item_id, quantity, price, subtotal, created_at, updated_at)
VALUES
(@order1_id, 3, 2, 100000, 200000, NOW(), NOW());

-- Đơn hàng 2: Đơn trung bình - Test các phương thức thanh toán
INSERT INTO orders (
    order_number, customer_id, status,
    subtotal, discount, tax, shipping_fee, grand_total,
    shipping_address, shipping_city, shipping_district, shipping_ward,
    shipping_phone, shipping_receiver_name,
    notes, created_by, updated_by, created_at, updated_at
) VALUES (
    'TEST002', @customer_id, 'PLACED',
    500000, 50000, 0, 30000, 480000,
    '456 Đường XYZ, Phường 2', 'TP. Hồ Chí Minh', 'Quận 3', 'Phường 2',
    '0907654321', 'Nguyễn Văn Test',
    'Đơn trung bình - Test VNPay/MoMo/SePay',
    'system', 'system', NOW(), NOW()
);

SET @order2_id = LAST_INSERT_ID();

-- Items cho order 2
INSERT INTO order_items (order_id, item_id, quantity, price, subtotal, created_at, updated_at)
VALUES
(@order2_id, 1, 2, 250000, 500000, NOW(), NOW());

-- Đơn hàng 3: Đơn lớn - Test phí COD tối đa
INSERT INTO orders (
    order_number, customer_id, status,
    subtotal, discount, tax, shipping_fee, grand_total,
    shipping_address, shipping_city, shipping_district, shipping_ward,
    shipping_phone, shipping_receiver_name,
    notes, created_by, updated_by, created_at, updated_at
) VALUES (
    'TEST003', @customer_id, 'PENDING_PAYMENT',
    3000000, 200000, 0, 50000, 2850000,
    '789 Đường DEF, Phường 3', 'TP. Hồ Chí Minh', 'Quận 5', 'Phường 3',
    '0909876543', 'Nguyễn Văn Test',
    'Đơn lớn - Test phí COD tối đa 50,000đ',
    'system', 'system', NOW(), NOW()
);

SET @order3_id = LAST_INSERT_ID();

-- Items cho order 3
INSERT INTO order_items (order_id, item_id, quantity, price, subtotal, created_at, updated_at)
VALUES
(@order3_id, 1, 12, 250000, 3000000, NOW(), NOW());

-- Đơn hàng 4: Đơn để test chuyển khoản
INSERT INTO orders (
    order_number, customer_id, status,
    subtotal, discount, tax, shipping_fee, grand_total,
    shipping_address, shipping_city, shipping_district, shipping_ward,
    shipping_phone, shipping_receiver_name,
    notes, created_by, updated_by, created_at, updated_at
) VALUES (
    'TEST004', @customer_id, 'PENDING_PAYMENT',
    800000, 0, 0, 40000, 840000,
    '321 Đường GHI, Phường 4', 'TP. Hồ Chí Minh', 'Quận 7', 'Phường 4',
    '0905555555', 'Nguyễn Văn Test',
    'Test chuyển khoản ngân hàng - Miễn phí',
    'system', 'system', NOW(), NOW()
);

SET @order4_id = LAST_INSERT_ID();

-- Items cho order 4
INSERT INTO order_items (order_id, item_id, quantity, price, subtotal, created_at, updated_at)
VALUES
(@order4_id, 2, 4, 150000, 600000, NOW(), NOW()),
(@order4_id, 3, 2, 100000, 200000, NOW(), NOW());

-- =============================================
-- 4. TẠO THANH TOÁN ĐÃ HOÀN THÀNH (ĐỂ TEST HOÀN TIỀN)
-- =============================================

-- Đơn hàng 5: Đã thanh toán, để test hoàn tiền
INSERT INTO orders (
    order_number, customer_id, status,
    subtotal, discount, tax, shipping_fee, grand_total,
    shipping_address, shipping_city, shipping_district, shipping_ward,
    shipping_phone, shipping_receiver_name,
    notes, created_by, updated_by, created_at, updated_at
) VALUES (
    'TEST005', @customer_id, 'PAID',
    1000000, 100000, 0, 50000, 950000,
    '999 Đường JKL, Phường 5', 'TP. Hồ Chí Minh', 'Quận 10', 'Phường 5',
    '0903333333', 'Nguyễn Văn Test',
    'Đơn đã thanh toán - Test hoàn tiền',
    'system', 'system', NOW(), NOW()
);

SET @order5_id = LAST_INSERT_ID();

-- Items cho order 5
INSERT INTO order_items (order_id, item_id, quantity, price, subtotal, created_at, updated_at)
VALUES
(@order5_id, 1, 4, 250000, 1000000, NOW(), NOW());

-- Payment đã hoàn thành cho order 5
INSERT INTO payments (
    order_id, method, status,
    amount, cod_fee, gateway_fee, transaction_fee, processing_fee, refunded_amount,
    transaction_id, gateway_transaction_id, gateway_response_code,
    is_paid, paid_at,
    notes, created_by, updated_by, created_at, updated_at
) VALUES (
    @order5_id, 'VNPAY', 'COMPLETED',
    950000, 0, 20900, 0, 0, 0,
    'TESTPAY001', 'VNP123456789', '00',
    TRUE, NOW(),
    'Thanh toán test thành công qua VNPay', 'customer', 'customer', NOW(), NOW()
);

SET @payment1_id = LAST_INSERT_ID();

-- =============================================
-- 5. TẠO YÊU CẦU HOÀN TIỀN TEST
-- =============================================

-- Yêu cầu hoàn tiền 1: Sản phẩm bị lỗi
INSERT INTO refunds (
    payment_id, refund_number,
    amount, refund_fee, status,
    reason, admin_notes,
    created_by, updated_by, created_at, updated_at
) VALUES (
    @payment1_id, 'TESTREF001',
    200000, 2000, 'PENDING',
    'Sản phẩm bị lỗi khi nhận hàng. 2/4 gấu bị rách, khách hàng yêu cầu hoàn tiền cho 2 sản phẩm lỗi.',
    'Đang chờ admin kiểm tra và xét duyệt',
    'customer', 'customer', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()
);

-- Yêu cầu hoàn tiền 2: Giao hàng chậm
INSERT INTO refunds (
    payment_id, refund_number,
    amount, refund_fee, status,
    reason, admin_notes,
    created_by, updated_by, created_at, updated_at
) VALUES (
    @payment1_id, 'TESTREF002',
    50000, 500, 'PENDING',
    'Giao hàng chậm 5 ngày so với cam kết. Yêu cầu bồi thường theo chính sách.',
    'Khách hàng yêu cầu bồi thường do giao hàng chậm',
    'customer', 'customer', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()
);

-- Payment thứ 2 để tạo thêm refund
INSERT INTO orders (
    order_number, customer_id, status,
    subtotal, discount, tax, shipping_fee, grand_total,
    shipping_address, shipping_city, shipping_district, shipping_ward,
    shipping_phone, shipping_receiver_name,
    notes, created_by, updated_by, created_at, updated_at
) VALUES (
    'TEST006', @customer_id, 'PAID',
    600000, 0, 0, 30000, 630000,
    '111 Đường MNO', 'TP. Hồ Chí Minh', 'Quận 11', 'Phường 1',
    '0906666666', 'Nguyễn Văn Test',
    'Đơn test hoàn tiền MoMo',
    'system', 'system', NOW(), NOW()
);

SET @order6_id = LAST_INSERT_ID();

INSERT INTO order_items (order_id, item_id, quantity, price, subtotal, created_at, updated_at)
VALUES (@order6_id, 2, 4, 150000, 600000, NOW(), NOW());

INSERT INTO payments (
    order_id, method, status,
    amount, cod_fee, gateway_fee, transaction_fee, processing_fee, refunded_amount,
    transaction_id, gateway_transaction_id, gateway_response_code,
    is_paid, paid_at,
    notes, created_by, updated_by, created_at, updated_at
) VALUES (
    @order6_id, 'MOMO', 'COMPLETED',
    630000, 0, 15750, 0, 0, 0,
    'TESTPAY002', 'MOMO987654321', '0',
    TRUE, NOW(),
    'Thanh toán test qua MoMo', 'customer', 'customer', NOW(), NOW()
);

SET @payment2_id = LAST_INSERT_ID();

-- Yêu cầu hoàn tiền 3: Đổi sản phẩm
INSERT INTO refunds (
    payment_id, refund_number,
    amount, refund_fee, status,
    reason, admin_notes,
    created_by, updated_by, created_at, updated_at
) VALUES (
    @payment2_id, 'TESTREF003',
    300000, 3000, 'PENDING',
    'Khách hàng muốn đổi 2 gấu sang loại khác. Hoàn tiền 2 sản phẩm.',
    NULL,
    'customer', 'customer', NOW(), NOW()
);

-- =============================================
-- 6. VERIFY DỮ LIỆU
-- =============================================

SELECT '=' as '', '=' as '', '=' as '', '=' as '', '=' as '';
SELECT '✓ DỮ LIỆU TEST ĐÃ TẠO XONG' as 'STATUS';
SELECT '=' as '', '=' as '', '=' as '', '=' as '', '=' as '';

SELECT CONCAT('Customer Test ID: ', @customer_id) as Info;

SELECT '' as '';
SELECT '📦 ĐƠN HÀNG CẦN THANH TOÁN (Customer)' as '';
SELECT
    order_number as 'Mã đơn',
    status as 'Trạng thái',
    CONCAT(FORMAT(subtotal, 0), ' VND') as 'Tiền hàng',
    CONCAT(FORMAT(grand_total, 0), ' VND') as 'Tổng cộng',
    notes as 'Ghi chú'
FROM orders
WHERE order_number LIKE 'TEST%'
  AND status IN ('PENDING_PAYMENT', 'PLACED')
ORDER BY order_number;

SELECT '' as '';
SELECT '💰 THANH TOÁN ĐÃ HOÀN THÀNH' as '';
SELECT
    p.transaction_id as 'Mã GD',
    o.order_number as 'Mã đơn',
    p.method as 'Phương thức',
    CONCAT(FORMAT(p.amount, 0), ' VND') as 'Số tiền',
    p.status as 'Trạng thái'
FROM payments p
JOIN orders o ON p.order_id = o.id
WHERE p.transaction_id LIKE 'TEST%';

SELECT '' as '';
SELECT '🔄 YÊU CẦU HOÀN TIỀN (Admin)' as '';
SELECT
    r.refund_number as 'Mã hoàn',
    CONCAT(FORMAT(r.amount, 0), ' VND') as 'Số tiền',
    r.status as 'Trạng thái',
    LEFT(r.reason, 50) as 'Lý do',
    DATE_FORMAT(r.created_at, '%d/%m/%Y') as 'Ngày tạo'
FROM refunds r
WHERE r.refund_number LIKE 'TEST%'
ORDER BY r.created_at DESC;

SELECT '' as '';
SELECT '=' as '', '=' as '', '=' as '', '=' as '', '=' as '';
SELECT '🎯 HƯỚNG DẪN TEST' as '';
SELECT '=' as '', '=' as '', '=' as '', '=' as '', '=' as '';

SELECT 'CUSTOMER - TEST THANH TOÁN:' as '';
SELECT '1. Login với username: customer_test (hoặc customer đã có)' as '';
SELECT '2. Click "💳 Thanh toán đơn hàng"' as '';
SELECT '3. Chọn đơn TEST001, TEST002, TEST003, TEST004' as '';
SELECT '4. Chọn phương thức thanh toán' as '';
SELECT '5. Xem phí tự động tính' as '';
SELECT '6. Click "THANH TOÁN NGAY"' as '';

SELECT '' as '';
SELECT 'ADMIN - TEST HOÀN TIỀN:' as '';
SELECT '1. Login với username: admin' as '';
SELECT '2. Click "Quản lý hoàn tiền"' as '';
SELECT '3. Xem 3 yêu cầu hoàn tiền đang chờ' as '';
SELECT '4. Chọn yêu cầu để xem chi tiết' as '';
SELECT '5. Click "Duyệt" hoặc "Từ chối"' as '';

SELECT '' as '';
SELECT '=' as '', '=' as '', '=' as '', '=' as '', '=' as '';
SELECT '📊 CHI TIẾT TEST CASE' as '';
SELECT '=' as '', '=' as '', '=' as '', '=' as '', '=' as '';

SELECT 'TEST001: Đơn nhỏ 200k' as 'Test Case', 'Phí COD = 10,000đ (min)' as 'Expected';
SELECT 'TEST002: Đơn trung 500k' as 'Test Case', 'Test VNPay (2.2%), MoMo (2.5%), SePay (1.8%)' as 'Expected';
SELECT 'TEST003: Đơn lớn 3M' as 'Test Case', 'Phí COD = 50,000đ (max)' as 'Expected';
SELECT 'TEST004: Đơn 800k' as 'Test Case', 'Chuyển khoản - Miễn phí' as 'Expected';
SELECT 'TEST005: Đã thanh toán' as 'Test Case', 'Có 2 yêu cầu hoàn tiền đang chờ' as 'Expected';

SELECT '' as '';
SELECT '✅ HOÀN TẤT! Bạn có thể test ngay bây giờ!' as '';
SELECT '=' as '', '=' as '', '=' as '', '=' as '', '=' as '';

-- =============================================
-- 7. SCRIPT XÓA DỮ LIỆU TEST (nếu cần)
-- =============================================

/*
-- Chạy các lệnh sau để xóa dữ liệu test:

DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE order_number LIKE 'TEST%');
DELETE FROM refunds WHERE refund_number LIKE 'TEST%';
DELETE FROM payments WHERE transaction_id LIKE 'TEST%';
DELETE FROM orders WHERE order_number LIKE 'TEST%';
DELETE FROM account_profiles WHERE account_id = (SELECT id FROM accounts WHERE username = 'customer_test');
DELETE FROM accounts WHERE username = 'customer_test';

SELECT '✓ Dữ liệu test đã xóa!' as Status;
*/

