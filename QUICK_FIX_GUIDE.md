# HƯỚNG DẪN NHANH - Khắc phục lỗi Order Management

## Bước 1: Chạy SQL Script (Quan trọng!)

### Cách 1: MySQL Command Line
```bash
mysql -u root -p shopgaubong < ensure_stock_items.sql
```

### Cách 2: MySQL Workbench / phpMyAdmin
1. Mở file `ensure_stock_items.sql`
2. Copy toàn bộ nội dung
3. Paste vào query window
4. Click Execute

### Cách 3: Chạy từng câu lệnh
```sql
-- 1. Tạo StockItem cho sản phẩm chưa có
INSERT INTO stock (warehouse_id, item_id, on_hand, reserved, low_stock_threshold, created_by, updated_by, created_at, updated_at)
SELECT 
    1 as warehouse_id,
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

-- 2. Cập nhật số lượng cho các sản phẩm (ví dụ)
UPDATE stock SET on_hand = 100 WHERE item_id IN (1, 2, 3, 4, 5);
```

## Bước 2: Restart ứng dụng

1. Dừng ứng dụng nếu đang chạy
2. Khởi động lại (Run trong IDE hoặc `java -jar`)

## Bước 3: Test các tính năng

### ✅ Test Order Management (Admin/Staff)
1. Login với tài khoản admin/staff
2. Vào "Order Management" / "Quản lý đơn hàng"
3. Click vào bất kỳ đơn hàng nào
4. **Kết quả mong đợi:** Hiển thị chi tiết đơn hàng, KHÔNG có lỗi LazyInitializationException

### ✅ Test Customer Orders
1. Login với tài khoản customer
2. Vào "My Orders" / "Đơn hàng của tôi"
3. Click vào bất kỳ đơn hàng nào
4. **Kết quả mong đợi:** Hiển thị chi tiết đơn hàng, KHÔNG có lỗi

### ✅ Test thêm sản phẩm vào giỏ hàng
1. Login với tài khoản customer
2. Vào "Products" / "Sản phẩm"
3. Thêm sản phẩm vào giỏ hàng
4. **Kết quả mong đợi:** 
   - Nếu đủ hàng: Thêm thành công
   - Nếu không đủ: Hiển thị thông báo "Không đủ hàng trong kho. Sản phẩm: XXX, Tồn kho khả dụng: 0, Yêu cầu: 1"

## Troubleshooting

### ❌ Vẫn bị lỗi "Sản phẩm không tồn tại trong kho"?

**Giải pháp 1:** Kiểm tra warehouse ID
```sql
-- Xem danh sách warehouse
SELECT * FROM warehouse;

-- Nếu không có warehouse nào, tạo mới
INSERT INTO warehouse (name, location, created_by, updated_by, created_at, updated_at)
VALUES ('Kho Chính', 'Hà Nội', 'admin', 'admin', NOW(), NOW());
```

**Giải pháp 2:** Kiểm tra StockItem
```sql
-- Xem sản phẩm nào chưa có trong kho
SELECT i.id, i.name, i.sku 
FROM item i 
WHERE NOT EXISTS (
    SELECT 1 FROM stock s 
    WHERE s.item_id = i.id AND s.warehouse_id = 1
);

-- Nếu có sản phẩm, chạy lại insert ở Bước 1
```

**Giải pháp 3:** Cập nhật số lượng tồn kho
```sql
-- Xem sản phẩm có số lượng = 0
SELECT s.*, i.name 
FROM stock s 
JOIN item i ON s.item_id = i.id 
WHERE s.on_hand = 0;

-- Cập nhật số lượng
UPDATE stock SET on_hand = 100 WHERE on_hand = 0;
```

### ❌ Vẫn bị LazyInitializationException?

1. **Restart lại ứng dụng** để load code mới
2. **Clear cache** của IDE:
   - IntelliJ IDEA: File → Invalidate Caches / Restart
   - Eclipse: Project → Clean
3. **Rebuild project:**
   - IntelliJ IDEA: Build → Rebuild Project
   - Eclipse: Project → Build All

### ❌ Không tìm thấy file SQL?

File nằm ở: `C:\Users\PC\eclipse-workspace\shopgaubong\ensure_stock_items.sql`

Hoặc copy nội dung này và chạy:
```sql
INSERT INTO stock (warehouse_id, item_id, on_hand, reserved, low_stock_threshold, created_by, updated_by, created_at, updated_at)
SELECT 
    1, i.id, 0, 0, 10, 'system', 'system', NOW(), NOW()
FROM item i
WHERE NOT EXISTS (SELECT 1 FROM stock s WHERE s.item_id = i.id AND s.warehouse_id = 1);
```

## Kiểm tra kết quả

### Query hữu ích

```sql
-- 1. Xem tất cả sản phẩm trong kho
SELECT 
    w.name as warehouse,
    i.name as product,
    i.sku,
    s.on_hand,
    s.reserved,
    (s.on_hand - s.reserved) as available
FROM stock s
JOIN warehouse w ON s.warehouse_id = w.id
JOIN item i ON s.item_id = i.id
ORDER BY w.name, i.name;

-- 2. Xem sản phẩm cần nhập kho
SELECT 
    i.name as product,
    s.on_hand,
    s.reserved
FROM stock s
JOIN item i ON s.item_id = i.id
WHERE (s.on_hand - s.reserved) < s.low_stock_threshold
ORDER BY (s.on_hand - s.reserved);

-- 3. Xem đơn hàng gần nhất
SELECT 
    o.order_number,
    o.status,
    a.username,
    COUNT(oi.id) as items,
    o.grand_total,
    o.created_at
FROM orders o
JOIN account a ON o.customer_id = a.id
LEFT JOIN order_item oi ON o.id = oi.order_id
GROUP BY o.id
ORDER BY o.created_at DESC
LIMIT 10;
```

## Ghi chú

- ✅ Code đã được cập nhật, không cần sửa gì thêm
- ✅ Chỉ cần chạy SQL script và restart app
- ✅ Tất cả lỗi đã được xử lý an toàn
- ✅ Thông báo lỗi giờ đây rõ ràng và hữu ích

## Liên hệ

Nếu vẫn gặp vấn đề, kiểm tra:
1. Log file: `logs/shopgaubong.log`
2. Console output khi chạy app
3. Database connection

