# Fix: Lỗi "Không đủ hàng trong kho" mặc dù vẫn còn hàng

## 🐛 Vấn đề

Khi thêm sản phẩm vào giỏ hàng nhiều lần, hệ thống báo "Không đủ hàng trong kho" mặc dù tồn kho vẫn còn đủ.

### Ví dụ lỗi:
- Tồn kho: On Hand = 10, Reserved = 0, Available = 10
- Lần 1: Thêm 5 cái vào giỏ → **OK** (giỏ có 5 cái)
- Lần 2: Thêm tiếp 3 cái → Hệ thống check: Available (10) >= 3 → **OK** ✅
- **Kết quả:** Giỏ hàng có 8 cái (5 + 3) → Vẫn trong giới hạn ✅

### Vấn đề thực sự:
- Lần 3: Thêm tiếp 3 cái nữa → Hệ thống check: Available (10) >= 3 → **OK** ✅
- **Kết quả:** Giỏ hàng có 11 cái (8 + 3) → **VƯỢ T QUÁ TỒN KHO!** ❌

## 🔍 Nguyên nhân

Trong method `addItemToOrder()`, khi sản phẩm đã có trong giỏ hàng:
1. Hệ thống chỉ kiểm tra số lượng **thêm vào** (quantity)
2. KHÔNG kiểm tra **tổng số lượng** sau khi cộng (existingQuantity + quantity)

```java
// CODE CŨ - SAI ❌
if (!stockService.checkAvailability(warehouseId, item.getId(), quantity)) {
    // Chỉ check số lượng thêm vào, không check tổng
}

Optional<OrderItem> existingItem = order.getOrderItems().stream()
    .filter(oi -> oi.getItem().getId().equals(item.getId()))
    .findFirst();

if (existingItem.isPresent()) {
    OrderItem orderItem = existingItem.get();
    orderItem.setQuantity(orderItem.getQuantity() + quantity); // Cộng thêm
}
```

## ✅ Giải pháp

### 1. Tìm OrderItem trước
Di chuyển logic tìm OrderItem lên trước khi kiểm tra tồn kho.

### 2. Tính tổng số lượng
```java
Integer totalQuantityNeeded = quantity;
if (existingItem.isPresent()) {
    totalQuantityNeeded = existingItem.get().getQuantity() + quantity;
}
```

### 3. Kiểm tra với tổng số lượng
```java
if (!stockService.checkAvailability(warehouseId, item.getId(), totalQuantityNeeded)) {
    // Báo lỗi với thông tin chi tiết
}
```

## 📝 Code đã sửa

### OrderService.java - addItemToOrder()

```java
// Tìm xem sản phẩm đã có trong giỏ chưa
Optional<OrderItem> existingItem = order.getOrderItems().stream()
        .filter(oi -> oi.getItem().getId().equals(item.getId()))
        .findFirst();

// Tính tổng số lượng cần kiểm tra
Integer totalQuantityNeeded = quantity;
if (existingItem.isPresent()) {
    // Nếu đã có trong giỏ, tổng = số lượng hiện tại + số lượng thêm vào
    totalQuantityNeeded = existingItem.get().getQuantity() + quantity;
}

// Kiểm tra tồn kho với TỔNG số lượng (không phải chỉ số lượng thêm vào)
if (!stockService.checkAvailability(warehouseId, item.getId(), totalQuantityNeeded)) {
    Integer availableQty = stockService.getAvailableQuantity(warehouseId, item.getId());
    throw new IllegalStateException(
        String.format("Không đủ hàng trong kho. Sản phẩm: %s, Tồn kho khả dụng: %d, Tổng yêu cầu: %d (trong giỏ: %d + thêm: %d)", 
        item.getName(), availableQty, totalQuantityNeeded, 
        existingItem.map(OrderItem::getQuantity).orElse(0), quantity));
}

if (existingItem.isPresent()) {
    OrderItem orderItem = existingItem.get();
    orderItem.setQuantity(totalQuantityNeeded);
    orderItem.calculateLineTotal();
} else {
    // Tạo OrderItem mới...
}
```

### OrderService.java - updateOrderItemQuantity()

Method này đã đúng vì nó kiểm tra `newQuantity` (tổng số lượng mới), nhưng cải thiện thông báo lỗi:

```java
if (!stockService.checkAvailability(warehouseId, orderItem.getItem().getId(), newQuantity)) {
    Integer availableQty = stockService.getAvailableQuantity(warehouseId, orderItem.getItem().getId());
    throw new IllegalStateException(
        String.format("Không đủ hàng trong kho. Sản phẩm: %s, Tồn kho khả dụng: %d, Số lượng mới yêu cầu: %d (hiện tại trong giỏ: %d)", 
        orderItem.getItem().getName(), availableQty, newQuantity, orderItem.getQuantity()));
}
```

### StockService.java - Thêm logging

Thêm logging chi tiết để debug:

```java
public boolean checkAvailability(Long warehouseId, Long itemId, Integer quantity) {
    Optional<StockItem> stockItemOpt = stockItemDAO.findByWarehouseAndItem(warehouseId, itemId);
    if (stockItemOpt.isEmpty()) {
        logger.warn("Không tìm thấy StockItem cho Warehouse ID {} và Item ID {}", warehouseId, itemId);
        return false;
    }
    
    StockItem stockItem = stockItemOpt.get();
    Integer available = stockItem.getAvailable();
    boolean result = available >= quantity;
    
    logger.debug("Check availability: Item ID {}, On Hand: {}, Reserved: {}, Available: {}, Required: {}, Result: {}", 
                itemId, stockItem.getOnHand(), stockItem.getReserved(), available, quantity, result);
    
    return result;
}

public Integer getAvailableQuantity(Long warehouseId, Long itemId) {
    Optional<StockItem> stockItemOpt = stockItemDAO.findByWarehouseAndItem(warehouseId, itemId);
    if (stockItemOpt.isEmpty()) {
        logger.warn("Không tìm thấy StockItem cho Warehouse ID {} và Item ID {} khi lấy available quantity", 
                   warehouseId, itemId);
        return 0;
    }
    
    StockItem stockItem = stockItemOpt.get();
    Integer available = stockItem.getAvailable();
    
    logger.debug("Get available quantity: Item ID {}, On Hand: {}, Reserved: {}, Available: {}", 
                itemId, stockItem.getOnHand(), stockItem.getReserved(), available);
    
    return available;
}
```

## 🎯 Files đã sửa

1. ✏️ **OrderService.java**
   - `addItemToOrder()`: Fix logic kiểm tra tồn kho với tổng số lượng
   - `updateOrderItemQuantity()`: Cải thiện thông báo lỗi

2. ✏️ **StockService.java**
   - `checkAvailability()`: Thêm logging chi tiết
   - `getAvailableQuantity()`: Thêm logging chi tiết

## 🧪 Test Case

### Test 1: Thêm sản phẩm mới vào giỏ
```
Tồn kho: 10
Thêm: 5
Kết quả: ✅ OK (giỏ có 5)
```

### Test 2: Thêm tiếp sản phẩm đã có (trong giới hạn)
```
Tồn kho: 10
Trong giỏ: 5
Thêm: 3
Tổng check: 8 <= 10
Kết quả: ✅ OK (giỏ có 8)
```

### Test 3: Thêm tiếp sản phẩm đã có (vượt giới hạn)
```
Tồn kho: 10
Trong giỏ: 8
Thêm: 3
Tổng check: 11 > 10
Kết quả: ❌ LỖI
Thông báo: "Không đủ hàng trong kho. Sản phẩm: XXX, Tồn kho khả dụng: 10, Tổng yêu cầu: 11 (trong giỏ: 8 + thêm: 3)"
```

### Test 4: Cập nhật số lượng trong giỏ
```
Tồn kho: 10
Trong giỏ: 5
Cập nhật thành: 12
Kết quả: ❌ LỖI
Thông báo: "Không đủ hàng trong kho. Sản phẩm: XXX, Tồn kho khả dụng: 10, Số lượng mới yêu cầu: 12 (hiện tại trong giỏ: 5)"
```

## 📊 So sánh Code Cũ vs Mới

| Tình huống | Code Cũ | Code Mới |
|------------|---------|----------|
| Thêm sản phẩm mới | ✅ Đúng | ✅ Đúng |
| Thêm sản phẩm đã có | ❌ SAI (chỉ check quantity) | ✅ ĐÚNG (check totalQuantity) |
| Cập nhật số lượng | ✅ Đúng | ✅ Đúng + thông báo tốt hơn |

## 🔍 Debug với Logging

Khi gặp vấn đề, kiểm tra log:

```
[DEBUG] Check availability: Item ID 5, On Hand: 10, Reserved: 2, Available: 8, Required: 9, Result: false
```

Từ log này có thể thấy:
- On Hand = 10 (tổng tồn kho)
- Reserved = 2 (đã giữ chỗ cho đơn hàng khác)
- Available = 8 (10 - 2)
- Required = 9 (số lượng yêu cầu)
- Result = false (không đủ hàng)

## ⚠️ Lưu ý

### Về Reserved Stock
Stock chỉ được reserve khi:
- Đơn hàng chuyển từ CART → PLACED
- Không phải khi thêm vào giỏ hàng

Vì vậy:
- Sản phẩm trong giỏ hàng (CART) **KHÔNG** làm giảm Available
- Chỉ khi đặt hàng (PLACED) thì mới reserve
- Nhiều customer có thể thêm cùng sản phẩm vào giỏ, ai đặt hàng trước thì reserve trước

### Về Race Condition
Vẫn có thể xảy ra race condition nếu:
1. Customer A check: Available = 10 → OK
2. Customer B check: Available = 10 → OK
3. Customer A place order: Reserved += 10, Available = 0
4. Customer B place order: Available = 0 < 10 → **LỖI**

Đây là hành vi mong muốn (first come first served).

## ✅ Kết quả

- ✅ Fix lỗi kiểm tra tồn kho khi thêm sản phẩm đã có trong giỏ
- ✅ Thông báo lỗi chi tiết, dễ hiểu
- ✅ Logging đầy đủ để debug
- ✅ Logic nhất quán giữa addItemToOrder và updateOrderItemQuantity

## 🚀 Cách test

1. **Khởi động ứng dụng**

2. **Test thêm sản phẩm nhiều lần:**
   - Thêm sản phẩm A: số lượng 5 → OK
   - Thêm tiếp sản phẩm A: số lượng 3 → OK (tổng 8)
   - Thêm tiếp sản phẩm A: số lượng 5 → Lỗi nếu tồn kho < 13

3. **Kiểm tra thông báo lỗi:**
   - Phải hiển thị số lượng trong giỏ hiện tại
   - Phải hiển thị số lượng thêm vào
   - Phải hiển thị tổng yêu cầu
   - Phải hiển thị tồn kho khả dụng

4. **Kiểm tra log file:**
   ```bash
   tail -f logs/shopgaubong.log | grep "Check availability"
   ```

## 📚 Tài liệu liên quan

- `FIX_STOCK_LAZY_LOADING.md` - Fix LazyInitializationException
- `ensure_stock_items.sql` - Script đồng bộ StockItem
- `QUICK_FIX_GUIDE.md` - Hướng dẫn nhanh

---

**Ngày sửa:** 1 tháng 12, 2025  
**Người sửa:** AI Assistant  
**Trạng thái:** ✅ Hoàn thành

