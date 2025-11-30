-- Script để đảm bảo tất cả sản phẩm đều có bản ghi trong bảng stock
-- Tạo StockItem cho các sản phẩm chưa có trong kho mặc định

-- Kiểm tra kho mặc định (giả sử ID = 1)
SELECT * FROM warehouse WHERE id = 1;

-- Xem các sản phẩm chưa có trong kho
SELECT i.id, i.name, i.sku
FROM item i
WHERE NOT EXISTS (
    SELECT 1 FROM stock s
    WHERE s.item_id = i.id AND s.warehouse_id = 1
);

-- Tạo StockItem cho các sản phẩm chưa có (với số lượng = 0)
INSERT INTO stock (warehouse_id, item_id, on_hand, reserved, low_stock_threshold, created_by, updated_by, created_at, updated_at)
SELECT
    1 as warehouse_id,  -- ID của kho mặc định
    i.id as item_id,
    0 as on_hand,
    0 as reserved,
    10 as low_stock_threshold,
    'system' as created_by,
    'system' as updated_by,
    NOW() as created_at,
    NOW() as updated_at
FROM item i
WHERE NOT EXISTS (
    SELECT 1 FROM stock s
    WHERE s.item_id = i.id AND s.warehouse_id = 1
);

-- Kiểm tra lại
SELECT
    s.id,
    w.name as warehouse_name,
    i.name as item_name,
    i.sku,
    s.on_hand,
    s.reserved,
    (s.on_hand - s.reserved) as available
FROM stock s
JOIN warehouse w ON s.warehouse_id = w.id
JOIN item i ON s.item_id = i.id
ORDER BY w.name, i.name;

-- Xem các sản phẩm có tồn kho = 0 (cần nhập kho)
SELECT
    w.name as warehouse_name,
    i.name as item_name,
    i.sku,
    s.on_hand,
    s.reserved,
    (s.on_hand - s.reserved) as available
FROM stock s
JOIN warehouse w ON s.warehouse_id = w.id
JOIN item i ON s.item_id = i.id
WHERE s.on_hand = 0
ORDER BY w.name, i.name;

