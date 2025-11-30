-- =============================================
-- Migration Script: Payment Feature Enhancement
-- Date: 2025-11-30
-- Description: Adds support for multiple payment gateways,
--              detailed fee tracking, and refund management
-- =============================================

-- 1. Update payments table with new columns
ALTER TABLE payments
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PENDING' AFTER method,
    ADD COLUMN transaction_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00 AFTER gateway_fee,
    ADD COLUMN processing_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00 AFTER transaction_fee,
    ADD COLUMN refunded_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 AFTER processing_fee,
    ADD COLUMN gateway_transaction_id VARCHAR(200) AFTER transaction_id,
    ADD COLUMN gateway_response_code VARCHAR(200) AFTER gateway_transaction_id,
    ADD COLUMN gateway_response TEXT AFTER gateway_response_code,
    ADD COLUMN expired_at DATETIME AFTER paid_at;

-- 2. Create index on transaction_id for faster lookup
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_gateway_transaction_id ON payments(gateway_transaction_id);

-- 3. Create refunds table
CREATE TABLE refunds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    refund_number VARCHAR(50) UNIQUE,
    amount DECIMAL(15,2) NOT NULL,
    refund_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reason TEXT NOT NULL,
    gateway_refund_id VARCHAR(200),
    gateway_response TEXT,
    approved_at DATETIME,
    approved_by VARCHAR(100),
    completed_at DATETIME,
    admin_notes TEXT,
    reject_reason TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    INDEX idx_refunds_payment_id (payment_id),
    INDEX idx_refunds_status (status),
    INDEX idx_refunds_refund_number (refund_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Update existing payment records
UPDATE payments
SET status = CASE
    WHEN is_paid = TRUE THEN 'COMPLETED'
    ELSE 'PENDING'
END
WHERE status IS NULL OR status = '';

-- 5. Add comments for documentation
ALTER TABLE payments
    MODIFY COLUMN status VARCHAR(30) NOT NULL COMMENT 'Trạng thái thanh toán: PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED, PARTIAL_REFUNDED',
    MODIFY COLUMN gateway_transaction_id VARCHAR(200) COMMENT 'Mã giao dịch từ cổng thanh toán',
    MODIFY COLUMN gateway_response_code VARCHAR(200) COMMENT 'Mã phản hồi từ gateway',
    MODIFY COLUMN gateway_response TEXT COMMENT 'Chi tiết phản hồi từ gateway',
    MODIFY COLUMN transaction_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Phí giao dịch',
    MODIFY COLUMN processing_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Phí xử lý',
    MODIFY COLUMN refunded_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Số tiền đã hoàn',
    MODIFY COLUMN expired_at DATETIME COMMENT 'Thời hạn thanh toán';

ALTER TABLE refunds
    MODIFY COLUMN status VARCHAR(30) NOT NULL COMMENT 'Trạng thái: PENDING, APPROVED, PROCESSING, COMPLETED, REJECTED, FAILED',
    MODIFY COLUMN gateway_refund_id VARCHAR(200) COMMENT 'Mã hoàn tiền từ gateway',
    MODIFY COLUMN gateway_response TEXT COMMENT 'Chi tiết phản hồi từ gateway khi hoàn tiền';

-- 6. Create view for payment summary
CREATE OR REPLACE VIEW v_payment_summary AS
SELECT
    p.id,
    p.transaction_id,
    p.method,
    p.status,
    p.amount,
    p.cod_fee,
    p.gateway_fee,
    p.transaction_fee,
    p.processing_fee,
    (p.cod_fee + p.gateway_fee + p.transaction_fee + p.processing_fee) as total_fees,
    (p.amount + p.cod_fee + p.gateway_fee + p.transaction_fee + p.processing_fee) as total_amount,
    p.refunded_amount,
    (p.amount - p.refunded_amount) as refundable_amount,
    p.is_paid,
    p.paid_at,
    p.expired_at,
    o.order_number,
    o.customer_id,
    a.username as customer_username,
    COUNT(r.id) as refund_count,
    COALESCE(SUM(r.amount), 0) as total_refunded
FROM payments p
JOIN orders o ON p.order_id = o.id
JOIN accounts a ON o.customer_id = a.id
LEFT JOIN refunds r ON p.id = r.payment_id
GROUP BY p.id;

-- 7. Create view for refund summary
CREATE OR REPLACE VIEW v_refund_summary AS
SELECT
    r.id,
    r.refund_number,
    r.payment_id,
    p.transaction_id as payment_transaction_id,
    p.method as payment_method,
    r.amount,
    r.refund_fee,
    (r.amount - r.refund_fee) as net_refund_amount,
    r.status,
    r.reason,
    r.approved_by,
    r.approved_at,
    r.completed_at,
    o.order_number,
    o.customer_id,
    a.username as customer_username,
    r.created_at as requested_at
FROM refunds r
JOIN payments p ON r.payment_id = p.id
JOIN orders o ON p.order_id = o.id
JOIN accounts a ON o.customer_id = a.id;

-- 8. Insert sample data (optional - for testing)
-- Uncomment if you want sample data

/*
-- Sample payment with VNPay
INSERT INTO payments (order_id, method, status, amount, gateway_fee, transaction_id, created_by, updated_by)
VALUES (1, 'VNPAY', 'PENDING', 500000, 11000, 'PAY1732924800000', 'system', 'system');

-- Sample payment with MoMo
INSERT INTO payments (order_id, method, status, amount, gateway_fee, transaction_id, created_by, updated_by)
VALUES (2, 'MOMO', 'COMPLETED', 300000, 7500, 'PAY1732924900000', 'system', 'system');

-- Sample COD payment
INSERT INTO payments (order_id, method, status, amount, cod_fee, transaction_id, is_paid, created_by, updated_by)
VALUES (3, 'COD', 'PENDING', 200000, 10000, 'PAY1732925000000', FALSE, 'system', 'system');

-- Sample refund
INSERT INTO refunds (payment_id, refund_number, amount, refund_fee, status, reason, created_by, updated_by)
VALUES (2, 'REF1732925100000', 100000, 1000, 'PENDING', 'Sản phẩm bị lỗi', 'customer', 'customer');
*/

-- =============================================
-- End of Migration Script
-- =============================================

