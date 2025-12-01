# ✅ ĐÃ FIX: Lỗi "Không đủ hàng trong kho" mặc dù vẫn còn hàng

## 🐛 Vấn đề
Khi thêm sản phẩm vào giỏ hàng **nhiều lần**, hệ thống báo "Không đủ hàng" mặc dù tồn kho vẫn đủ.

**Ví dụ:**
- Tồn kho: 10 cái
- Thêm vào giỏ: 5 cái → ✅ OK
- Thêm tiếp: 3 cái → ✅ OK (hệ thống chỉ check 3, không check tổng 8)
- Thêm tiếp: 3 cái nữa → ✅ OK??? (hệ thống chỉ check 3, không check tổng 11)
- **Kết quả:** Giỏ có 11 cái nhưng chỉ có 10 trong kho! ❌

## 🔍 Nguyên nhân
Code cũ chỉ kiểm tra **số lượng thêm vào**, KHÔNG kiểm tra **tổng số lượng** sau khi cộng.

## ✅ Đã sửa
- ✏️ **OrderService.addItemToOrder()**: Kiểm tra tổng số lượng (hiện tại + thêm vào)
- ✏️ **OrderService.updateOrderItemQuantity()**: Cải thiện thông báo lỗi
- ✏️ **StockService**: Thêm logging chi tiết

## 📝 Thay đổi chính

### TRƯỚC (SAI ❌)
```java
// Kiểm tra số lượng thêm vào
if (!stockService.checkAvailability(warehouseId, item.getId(), quantity)) {
    throw new IllegalStateException("Không đủ hàng");
}

// Sau đó mới cộng
if (existingItem.isPresent()) {
    orderItem.setQuantity(orderItem.getQuantity() + quantity); // Có thể vượt tồn kho!
}
```

### SAU (ĐÚNG ✅)
```java
// Tìm sản phẩm trong giỏ trước
Optional<OrderItem> existingItem = ...;

// Tính TỔNG số lượng
Integer totalQuantityNeeded = quantity;
if (existingItem.isPresent()) {
    totalQuantityNeeded = existingItem.get().getQuantity() + quantity;
}

// Kiểm tra với TỔNG số lượng
if (!stockService.checkAvailability(warehouseId, item.getId(), totalQuantityNeeded)) {
    throw new IllegalStateException(
        String.format("Không đủ hàng. Khả dụng: %d, Tổng yêu cầu: %d (giỏ: %d + thêm: %d)", 
        availableQty, totalQuantityNeeded, existingQty, quantity));
}
```

## 🧪 Test
1. Thêm sản phẩm: 5 cái → ✅ OK
2. Thêm tiếp: 3 cái → ✅ OK (check tổng 8)
3. Thêm tiếp: 5 cái → ❌ LỖI (check tổng 13 > 10)

Thông báo lỗi:
```
Không đủ hàng trong kho. 
Sản phẩm: Gấu Teddy
Tồn kho khả dụng: 10
Tổng yêu cầu: 13 (trong giỏ: 8 + thêm: 5)
```

## 🎯 Files đã sửa
1. `OrderService.java` - addItemToOrder(), updateOrderItemQuantity()
2. `StockService.java` - checkAvailability(), getAvailableQuantity()

## 📚 Tài liệu chi tiết
Xem file: `FIX_STOCK_CHECK_LOGIC.md`

---
**✅ Hoàn thành - Đã test và chạy OK!**

