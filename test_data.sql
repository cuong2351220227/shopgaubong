-- =============================================
-- Test Data for Payment Feature
-- =============================================

-- Tạo đơn hàng test cho customer (giả sử customer có id = 2)
-- Thay customer_id phù hợp với database của bạn

-- Xóa dữ liệu test cũ (nếu có)
DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE order_number LIKE 'TEST%');
DELETE FROM payments WHERE order_id IN (SELECT id FROM orders WHERE order_number LIKE 'TEST%');
DELETE FROM orders WHERE order_number LIKE 'TEST%';

-- Tạo đơn hàng test 1 - Chờ thanh toán
INSERT INTO orders (
    order_number,
    customer_id,
    status,
    subtotal,
    discount,
    tax,
    shipping_fee,
    grand_total,
    shipping_address,
    shipping_city,
    shipping_phone,
    shipping_receiver_name,
    notes,
    created_by,
    updated_by,
    created_at,
    updated_at
) VALUES (
    'TEST001',
    2, -- Thay bằng ID customer của bạn
    'PENDING_PAYMENT',
    500000,
    0,
    0,
    30000,
    530000,
    '123 Đường ABC, Quận 1',
    'TP. Hồ Chí Minh',
    '0901234567',
    'Nguyễn Văn A',
    'Đơn hàng test thanh toán',
    'system',
    'system',
    NOW(),
    NOW()
);

-- Tạo đơn hàng test 2 - Đã đặt hàng
INSERT INTO orders (
    order_number,
    customer_id,
    status,
    subtotal,
    discount,
    tax,
    shipping_fee,
    grand_total,
    shipping_address,
    shipping_city,
    shipping_phone,
    shipping_receiver_name,
    notes,
    created_by,
    updated_by,
    created_at,
    updated_at
) VALUES (
    'TEST002',
    2, -- Thay bằng ID customer của bạn
    'PLACED',
    1000000,
    100000,
    0,
    50000,
    950000,
    '456 Đường XYZ, Quận 3',
    'TP. Hồ Chí Minh',
    '0907654321',
    'Trần Thị B',
    'Đơn hàng test thanh toán gateway',
    'system',
    'system',
    NOW(),
    NOW()
);

-- Tạo đơn hàng test 3 - Đơn nhỏ để test phí COD
INSERT INTO orders (
    order_number,
    customer_id,
    status,
    subtotal,
    discount,
    tax,
    shipping_fee,
    grand_total,
    shipping_address,
    shipping_city,
    shipping_phone,
    shipping_receiver_name,
    notes,
    created_by,
    updated_by,
    created_at,
    updated_at
) VALUES (
    'TEST003',
    2, -- Thay bằng ID customer của bạn
    'PENDING_PAYMENT',
    200000,
    0,
    0,
    20000,
    220000,
    '789 Đường DEF, Quận 5',
    'TP. Hồ Chí Minh',
    '0909876543',
    'Lê Văn C',
    'Đơn hàng nhỏ test phí COD',
    'system',
    'system',
    NOW(),
    NOW()
);

-- =============================================
-- Test Data for Refund Feature
-- =============================================

-- Tạo payment đã hoàn thành để test hoàn tiền
INSERT INTO payments (
    order_id,
    method,
    status,
    amount,
    cod_fee,
    gateway_fee,
    transaction_fee,
    processing_fee,
    refunded_amount,
    transaction_id,
    gateway_transaction_id,
    is_paid,
    paid_at,
    notes,
    created_by,
    updated_by,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM orders WHERE order_number = 'TEST002'),
    'VNPAY',
    'COMPLETED',
    950000,
    0,
    20900,
    0,
    0,
    0,
    'TESTPAY001',
    'VNP123456789',
    TRUE,
    NOW(),
    'Thanh toán test để tạo hoàn tiền',
    'system',
    'system',
    NOW(),
    NOW()
);

-- Tạo yêu cầu hoàn tiền test
INSERT INTO refunds (
    payment_id,
    refund_number,
    amount,
    refund_fee,
    status,
    reason,
    admin_notes,
    created_by,
    updated_by,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM payments WHERE transaction_id = 'TESTPAY001'),
    'TESTREF001',
    100000,
    1000,
    'PENDING',
    'Sản phẩm bị lỗi, khách hàng yêu cầu hoàn một phần tiền',
    'Đang chờ admin xử lý',
    'customer',
    'customer',
    NOW(),
    NOW()
);

-- Tạo thêm một yêu cầu hoàn tiền khác
INSERT INTO refunds (
    payment_id,
    refund_number,
    amount,
    refund_fee,
    status,
    reason,
    admin_notes,
    created_by,
    updated_by,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM payments WHERE transaction_id = 'TESTPAY001'),
    'TESTREF002',
    50000,
    500,
    'PENDING',
    'Giao hàng chậm, khách hàng yêu cầu bồi thường',
    'Đang chờ admin xét duyệt',
    'customer',
    'customer',
    NOW(),
    NOW()
);

-- =============================================
-- Verify Data
-- =============================================

SELECT 'Test Orders Created:' AS info;
SELECT order_number, customer_id, status, grand_total
FROM orders
WHERE order_number LIKE 'TEST%';

SELECT 'Test Payments Created:' AS info;
SELECT transaction_id, method, status, amount
FROM payments
WHERE transaction_id LIKE 'TEST%';

SELECT 'Test Refunds Created:' AS info;
SELECT refund_number, amount, status, reason
FROM refunds
WHERE refund_number LIKE 'TEST%';

-- =============================================
-- Lưu ý:
-- 1. Thay customer_id = 2 bằng ID customer thực tế
-- 2. Chạy script này sau khi chạy payment_migration.sql
-- 3. Để xóa dữ liệu test:
--    DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE order_number LIKE 'TEST%');
--    DELETE FROM refunds WHERE refund_number LIKE 'TEST%';
--    DELETE FROM payments WHERE transaction_id LIKE 'TEST%';
--    DELETE FROM orders WHERE order_number LIKE 'TEST%';
-- =============================================

