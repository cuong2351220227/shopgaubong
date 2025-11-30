# Giải Quyết Vấn Đề "Không Đủ Hàng Trong Kho"

## Ngày: 1 Tháng 12, 2025

## 🔍 PHÂN TÍCH VẤN ĐỀ

### Triệu chứng
- Khách hàng không thể thêm sản phẩm vào giỏ hàng
- Lỗi xuất hiện: **"Không đủ hàng trong kho"**
- Log hiển thị:
  ```
  ERROR c.e.shopgaubong.service.OrderService - Lỗi khi thêm sản phẩm vào đơn hàng: Không đủ hàng trong kho
  ```

### Nguyên nhân gốc rễ

#### 1. **Không có kho (Warehouse) trong database**
- Log cho thấy: `Tải kho thành công: 0 kho`
- Hệ thống cần ít nhất 1 kho để lưu trữ sản phẩm

#### 2. **Không có dữ liệu tồn kho (StockItem) trong database**
- Log cho thấy: `Loaded 0 stock items`
- Mỗi sản phẩm cần có record trong bảng `stock_items` để theo dõi số lượng tồn kho

#### 3. **Luồng kiểm tra tồn kho trong OrderService**

Khi thêm sản phẩm vào giỏ hàng, `ProductCatalogController` gọi:

```java
// ProductCatalogController.java (line 209)
orderService.addItemToOrder(cart.getId(), selectedItem, quantity, DEFAULT_WAREHOUSE_ID);
```

Trong `OrderService.addItemToOrder()`:

```java
// OrderService.java (line 69-71)
if (!stockService.checkAvailability(warehouseId, item.getId(), quantity)) {
    throw new IllegalStateException("Không đủ hàng trong kho");
}
```

Phương thức `StockService.checkAvailability()`:

```java
// StockService.java (line 277-282)
public boolean checkAvailability(Long warehouseId, Long itemId, Integer quantity) {
    Optional<StockItem> stockItemOpt = stockItemDAO.findByWarehouseAndItem(warehouseId, itemId);
    if (stockItemOpt.isEmpty()) {
        return false;  // ❌ Trả về false vì không tìm thấy StockItem
    }
    return stockItemOpt.get().getAvailable() >= quantity;
}
```

#### 4. **Logic kiểm tra tồn kho trong StockItem entity**

```java
// StockItem.java
public Integer getAvailable() {
    return onHand - reserved;  // Số lượng có sẵn = Tồn kho - Đã đặt trước
}

public void reserveStock(Integer quantity) {
    if (getAvailable() < quantity) {
        throw new IllegalStateException("Không đủ hàng tồn kho");
    }
    this.reserved += quantity;
}
```

### Vấn đề trong DatabaseInitializer

File `DatabaseInitializer.java` ban đầu chỉ tạo tài khoản:

```java
public static void initializeSampleData() {
    logger.info("Bắt đầu khởi tạo dữ liệu mẫu...");
    try {
        createDefaultAccounts();  // ✅ Chỉ tạo tài khoản
        logger.info("Hoàn tất khởi tạo dữ liệu mẫu");
    } catch (Exception e) {
        logger.error("Lỗi khi khởi tạo dữ liệu mẫu", e);
    }
}
```

**❌ Thiếu:**
- Tạo warehouse (kho)
- Tạo stock items (tồn kho cho mỗi sản phẩm)

## ✅ GIẢI PHÁP

### Cải tiến DatabaseInitializer

Đã thêm 2 phương thức mới vào `DatabaseInitializer.java`:

#### 1. `createDefaultWarehouse()` - Tạo kho mặc định

```java
private static void createDefaultWarehouse() {
    WarehouseService warehouseService = new WarehouseService();
    
    try {
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        if (warehouses.isEmpty()) {
            warehouseService.createWarehouse(
                "KHO-001",              // Mã kho
                "Kho Trung Tâm",       // Tên kho
                "123 Đường ABC",        // Địa chỉ
                "TP. Hồ Chí Minh",     // Thành phố
                "Quận 1",               // Quận
                "Phường Bến Nghé",     // Phường
                "0901234567"            // Số điện thoại
            );
            logger.info("Tạo kho mặc định: KHO-001 - Kho Trung Tâm");
        }
    } catch (Exception e) {
        logger.error("Lỗi khi tạo kho mặc định", e);
    }
}
```

**Kết quả:**
- Tạo 1 kho mặc định với ID = 1 (tự động tăng)
- Kho này sẽ được sử dụng làm `DEFAULT_WAREHOUSE_ID` trong `ProductCatalogController`

#### 2. `createDefaultStockItems()` - Tạo tồn kho cho tất cả sản phẩm

```java
private static void createDefaultStockItems() {
    StockService stockService = new StockService();
    ItemService itemService = new ItemService();
    WarehouseService warehouseService = new WarehouseService();
    
    try {
        // Lấy kho đầu tiên
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        if (warehouses.isEmpty()) {
            logger.warn("Không tìm thấy kho nào để tạo tồn kho");
            return;
        }
        
        Warehouse defaultWarehouse = warehouses.getFirst();
        logger.info("Sử dụng kho: {} (ID: {})", 
                   defaultWarehouse.getName(), defaultWarehouse.getId());
        
        // Lấy tất cả sản phẩm active
        List<Item> items = itemService.getActiveItems();
        if (items.isEmpty()) {
            logger.warn("Không có sản phẩm nào để tạo tồn kho");
            return;
        }
        
        int createdCount = 0;
        for (Item item : items) {
            try {
                // Kiểm tra stock item đã tồn tại chưa
                List<StockItem> existingStock = 
                    stockService.getStockItemsByItem(item.getId());
                    
                if (existingStock.isEmpty()) {
                    stockService.createStockItem(
                        defaultWarehouse.getId(),
                        item.getId(),
                        100,  // ✅ Số lượng tồn kho ban đầu: 100 đơn vị
                        10    // Ngưỡng cảnh báo tồn kho thấp: 10 đơn vị
                    );
                    createdCount++;
                    logger.info("Tạo tồn kho cho sản phẩm: {} (100 đơn vị)", 
                               item.getName());
                }
            } catch (IllegalArgumentException e) {
                logger.debug("Tồn kho đã tồn tại cho sản phẩm: {}", 
                            item.getName());
            }
        }
        
        logger.info("Đã tạo {} mục tồn kho trong kho {}", 
                   createdCount, defaultWarehouse.getName());
    } catch (Exception e) {
        logger.error("Lỗi khi tạo dữ liệu tồn kho mặc định", e);
    }
}
```

**Kết quả:**
- Tạo StockItem cho mỗi sản phẩm active trong hệ thống
- Mỗi StockItem có:
  - `onHand`: 100 (số lượng tồn kho)
  - `reserved`: 0 (số lượng đã đặt trước)
  - `lowStockThreshold`: 10 (ngưỡng cảnh báo)
  - `available`: 100 - 0 = 100 (số lượng có sẵn để bán)

#### 3. Cập nhật phương thức `initializeSampleData()`

```java
public static void initializeSampleData() {
    logger.info("Bắt đầu khởi tạo dữ liệu mẫu...");
    
    try {
        createDefaultAccounts();      // ✅ Tạo tài khoản
        createDefaultWarehouse();     // ✅ Tạo kho
        createDefaultStockItems();    // ✅ Tạo tồn kho
        logger.info("Hoàn tất khởi tạo dữ liệu mẫu");
    } catch (Exception e) {
        logger.error("Lỗi khi khởi tạo dữ liệu mẫu", e);
    }
}
```

## 📊 LUỒNG DỮ LIỆU SAU KHI SỬA

### 1. Khởi động ứng dụng

```
Launcher.main()
  └─> DatabaseInitializer.initializeSampleData()
        ├─> createDefaultAccounts()     → Tạo admin, staff, customer
        ├─> createDefaultWarehouse()    → Tạo "KHO-001"
        └─> createDefaultStockItems()   → Tạo 100 đơn vị cho mỗi sản phẩm
```

### 2. Khách hàng thêm sản phẩm vào giỏ

```
ProductCatalogController.handleAddToCart()
  └─> OrderService.addItemToOrder(orderId, item, quantity, warehouseId=1)
        └─> StockService.checkAvailability(warehouseId=1, itemId, quantity)
              └─> StockItemDAO.findByWarehouseAndItem(1, itemId)
                    └─> ✅ Tìm thấy StockItem
                          └─> stockItem.getAvailable() = 100 - 0 = 100
                                └─> 100 >= quantity → ✅ return true
```

### 3. Đặt hàng (place order)

```
OrderService.placeOrder()
  └─> For each OrderItem:
        └─> StockService.reserveStock(warehouseId=1, itemId, quantity)
              └─> StockItem.reserveStock(quantity)
                    └─> onHand=100, reserved=0
                          └─> reserved += quantity (e.g., reserved=5)
                                └─> available = 100 - 5 = 95
```

### 4. Giao hàng (ship order)

```
OrderService.shipOrder()
  └─> For each OrderItem:
        └─> StockService.commitStock(warehouseId=1, itemId, quantity)
              └─> StockItem.commitStock(quantity)
                    └─> onHand=100, reserved=5
                          └─> reserved -= quantity (reserved=0)
                          └─> onHand -= quantity (onHand=95)
                                └─> available = 95 - 0 = 95
```

## 🎯 KẾT QUẢ

### Trước khi sửa
- ❌ 0 warehouse
- ❌ 0 stock items
- ❌ Không thể thêm sản phẩm vào giỏ hàng
- ❌ Lỗi: "Không đủ hàng trong kho"

### Sau khi sửa
- ✅ 1 warehouse (KHO-001 - Kho Trung Tâm)
- ✅ N stock items (100 đơn vị cho mỗi sản phẩm active)
- ✅ Khách hàng có thể thêm sản phẩm vào giỏ hàng
- ✅ Hệ thống theo dõi tồn kho chính xác

## 🚀 HƯỚNG DẪN SỬ DỤNG

### Lần chạy đầu tiên (Fresh start)

1. **Xóa database cũ** (nếu có):
   ```sql
   DROP DATABASE IF EXISTS shopgaubong;
   CREATE DATABASE shopgaubong 
       CHARACTER SET utf8mb4 
       COLLATE utf8mb4_unicode_ci;
   ```

2. **Chạy ứng dụng**:
   ```
   mvn clean javafx:run
   ```

3. **Xem log khởi tạo**:
   ```
   [main] INFO  c.e.s.util.DatabaseInitializer - Bắt đầu khởi tạo dữ liệu mẫu...
   [main] INFO  c.e.s.util.DatabaseInitializer - Tạo tài khoản Admin mặc định: admin/admin123
   [main] INFO  c.e.s.util.DatabaseInitializer - Tạo tài khoản Staff mặc định: staff/staff123
   [main] INFO  c.e.s.util.DatabaseInitializer - Tạo tài khoản Customer mặc định: customer/customer123
   [main] INFO  c.e.s.util.DatabaseInitializer - Tạo kho mặc định: KHO-001 - Kho Trung Tâm
   [main] INFO  c.e.s.util.DatabaseInitializer - Sử dụng kho: Kho Trung Tâm (ID: 1)
   [main] INFO  c.e.s.util.DatabaseInitializer - Tạo tồn kho cho sản phẩm: Gấu Bông Teddy (100 đơn vị)
   [main] INFO  c.e.s.util.DatabaseInitializer - Tạo tồn kho cho sản phẩm: Gấu Bông Panda (100 đơn vị)
   ...
   [main] INFO  c.e.s.util.DatabaseInitializer - Đã tạo 10 mục tồn kho trong kho Kho Trung Tâm
   [main] INFO  c.e.s.util.DatabaseInitializer - Hoàn tất khởi tạo dữ liệu mẫu
   ```

4. **Đăng nhập và test**:
   - Login: `customer` / `customer123`
   - Thêm sản phẩm vào giỏ hàng
   - ✅ Thành công!

### Kiểm tra database

```sql
-- Kiểm tra kho
SELECT * FROM warehouses;
-- Kết quả: 1 row (KHO-001)

-- Kiểm tra tồn kho
SELECT 
    si.id,
    w.name AS warehouse,
    i.name AS item,
    si.on_hand,
    si.reserved,
    (si.on_hand - si.reserved) AS available
FROM stock_items si
JOIN warehouses w ON si.warehouse_id = w.id
JOIN items i ON si.item_id = i.id;
-- Kết quả: N rows (mỗi sản phẩm có 100 đơn vị)
```

## 📝 LƯU Ý

1. **Tính idempotent**: 
   - Các phương thức khởi tạo kiểm tra dữ liệu đã tồn tại trước khi tạo mới
   - An toàn khi chạy nhiều lần

2. **Tùy chỉnh số lượng tồn kho**:
   - Hiện tại: 100 đơn vị/sản phẩm
   - Có thể thay đổi trong `createDefaultStockItems()`

3. **Quản lý nhiều kho**:
   - Hiện tại: 1 kho mặc định
   - Có thể thêm nhiều kho qua giao diện quản lý

4. **Low stock warning**:
   - Ngưỡng: 10 đơn vị
   - Khi `available <= 10`, hệ thống sẽ cảnh báo

## 🔗 FILE LIÊN QUAN

- ✏️ `DatabaseInitializer.java` - File đã sửa
- 📖 `StockService.java` - Logic quản lý tồn kho
- 📖 `StockItem.java` - Entity tồn kho
- 📖 `OrderService.java` - Logic đặt hàng
- 📖 `ProductCatalogController.java` - Giao diện thêm vào giỏ

---
**Tác giả**: GitHub Copilot  
**Ngày tạo**: 1 Tháng 12, 2025

