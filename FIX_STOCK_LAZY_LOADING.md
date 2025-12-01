# Hướng Dẫn Khắc Phục Lỗi Stock Management

## Tóm tắt các vấn đề đã sửa

### 1. LazyInitializationException trong Order Management
**Vấn đề:** Khi truy cập các thuộc tính lazy-loaded (orderItems, customer, etc.) sau khi Hibernate session đã đóng.

**Giải pháp:**
- Thêm eager loading (LEFT JOIN FETCH) trong tất cả các query methods
- Tạo phương thức `getOrderWithFullDetails()` để load đầy đủ thông tin
- Cập nhật controllers để sử dụng phương thức này khi hiển thị chi tiết đơn hàng

**Files đã sửa:**
- `OrderService.java`: Thêm các phương thức với eager loading
- `OrderDAO.java`: Override findAll() và findById() với JOIN FETCH
- `OrderManagementController.java`: Load lại order với full details khi click
- `CustomerOrderController.java`: Tương tự OrderManagementController
- `CartService.java`: Load cart với full details

### 2. IllegalStateException: "Sản phẩm không tồn tại trong kho"
**Vấn đề:** Khi thêm sản phẩm vào đơn hàng nhưng chưa có bản ghi StockItem trong database.

**Nguyên nhân:**
- Sản phẩm (Item) đã được tạo nhưng chưa được nhập vào kho (chưa có record trong bảng `stock`)
- Hệ thống không tự động tạo StockItem khi tạo Item mới

**Giải pháp:**

#### A. Tự động tạo StockItem (Đã implement)
Cập nhật `StockService.reserveStock()`:
- Tự động tạo StockItem với số lượng = 0 nếu chưa tồn tại
- Kiểm tra tồn kho khả dụng trước khi reserve
- Thông báo lỗi chi tiết (tên sản phẩm, số lượng khả dụng, số lượng yêu cầu)

#### B. Cải thiện các phương thức khác
- `releaseStock()`: Không throw exception nếu StockItem không tồn tại
- `commitStock()`: Kiểm tra số lượng reserved trước khi commit
- Thêm các utility methods: `getAvailableQuantity()`, `isItemInWarehouse()`, `getOrCreateStockItem()`

#### C. Cải thiện thông báo lỗi
- `addItemToOrder()`: Hiển thị số lượng khả dụng trong thông báo lỗi
- `updateOrderItemQuantity()`: Tương tự addItemToOrder

**Files đã sửa:**
- `StockService.java`: Tự động tạo StockItem, cải thiện error messages
- `OrderService.java`: Cải thiện error messages

### 3. Script SQL để đảm bảo dữ liệu nhất quán
**File mới:** `ensure_stock_items.sql`

Chạy script này để:
1. Kiểm tra các sản phẩm chưa có trong kho
2. Tự động tạo StockItem cho tất cả sản phẩm (với số lượng = 0)
3. Xem danh sách sản phẩm cần nhập kho

## Cách sử dụng

### Bước 1: Chạy Script SQL
```bash
mysql -u root -p shopgaubong < ensure_stock_items.sql
```

Hoặc trong MySQL Workbench/phpMyAdmin, mở và execute file `ensure_stock_items.sql`

### Bước 2: Nhập kho cho sản phẩm
Sau khi có StockItem, cần cập nhật số lượng tồn kho:

```sql
-- Cập nhật số lượng tồn kho cho sản phẩm
UPDATE stock 
SET on_hand = 100,  -- Số lượng nhập kho
    updated_by = 'admin',
    updated_at = NOW()
WHERE warehouse_id = 1 AND item_id = [ID_SẢN_PHẨM];
```

### Bước 3: Kiểm tra trong ứng dụng
1. Mở module **Stock Management** (Quản lý tồn kho)
2. Kiểm tra danh sách sản phẩm có trong kho
3. Nhập kho cho các sản phẩm có số lượng = 0

### Bước 4: Test thêm sản phẩm vào đơn hàng
1. Đăng nhập với tài khoản customer
2. Thêm sản phẩm vào giỏ hàng
3. Nếu không đủ hàng, hệ thống sẽ hiển thị thông báo rõ ràng:
   - Tên sản phẩm
   - Số lượng khả dụng
   - Số lượng yêu cầu

## Luồng xử lý mới

### Khi thêm sản phẩm vào giỏ hàng:
1. **Kiểm tra StockItem tồn tại:**
   - Nếu không → Tự động tạo với số lượng = 0
   
2. **Kiểm tra tồn kho khả dụng:**
   - Available = On Hand - Reserved
   - Nếu Available < Quantity yêu cầu → Báo lỗi chi tiết
   
3. **Reserve stock:**
   - Reserved += Quantity
   - Available giảm tương ứng

### Khi đặt hàng (Place Order):
1. Chuyển trạng thái từ CART → PLACED
2. Stock vẫn ở trạng thái Reserved

### Khi giao hàng (Delivered):
1. Commit stock: Reserved → On Hand thực sự giảm
2. Reserved giảm, On Hand giảm

### Khi hủy đơn hàng:
1. Release stock: Reserved giảm
2. Available tăng lại

## Lợi ích của giải pháp

1. **Tự động hóa:** Không cần lo lắng về việc tạo StockItem thủ công
2. **Thông báo rõ ràng:** User biết chính xác vì sao không thể thêm sản phẩm
3. **Dữ liệu nhất quán:** Script SQL giúp đồng bộ dữ liệu hiện tại
4. **Xử lý lỗi an toàn:** Không crash app khi StockItem không tồn tại
5. **Quản lý tốt hơn:** Admin có thể theo dõi sản phẩm cần nhập kho

## Troubleshooting

### Vẫn gặp lỗi "Không đủ hàng trong kho"?
1. Kiểm tra số lượng tồn kho:
```sql
SELECT 
    i.name,
    s.on_hand,
    s.reserved,
    (s.on_hand - s.reserved) as available
FROM stock s
JOIN item i ON s.item_id = i.id
WHERE i.id = [ID_SẢN_PHẨM];
```

2. Nhập kho nếu cần:
   - Vào module Stock Management
   - Chọn warehouse
   - Add/Update stock cho sản phẩm

### Sản phẩm mới không hiển thị trong kho?
1. Chạy lại script `ensure_stock_items.sql`
2. Hoặc thêm thủ công trong Stock Management

### Lỗi "Kho không tồn tại"?
1. Kiểm tra warehouse ID trong code
2. Đảm bảo có ít nhất 1 warehouse trong DB:
```sql
SELECT * FROM warehouse;
```

3. Nếu chưa có, tạo warehouse mặc định:
```sql
INSERT INTO warehouse (name, location, created_by, updated_by, created_at, updated_at)
VALUES ('Kho Chính', 'Hà Nội', 'system', 'system', NOW(), NOW());
```

## Ghi chú cho Developer

- **Không xóa:** Các phương thức utility đã thêm có thể hữu ích sau này
- **Logging:** Đã thêm logging chi tiết để debug dễ dàng
- **Transaction:** Tất cả operations đều có transaction management
- **Eager Loading:** OrderDAO và OrderService đều dùng JOIN FETCH để tránh N+1 queries

## Testing Checklist

- [ ] Tạo sản phẩm mới và thêm vào giỏ hàng (auto-create StockItem)
- [ ] Thêm sản phẩm vượt quá tồn kho (xem error message)
- [ ] Hủy đơn hàng (release stock correctly)
- [ ] Giao hàng (commit stock correctly)
- [ ] Xem Order Management (no LazyInitializationException)
- [ ] Xem Customer Orders (no LazyInitializationException)
- [ ] Checkout process (cart loads correctly)

