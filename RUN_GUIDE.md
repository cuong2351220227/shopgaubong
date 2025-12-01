# 🚀 HƯỚNG DẪN CHẠY ỨNG DỤNG SAU KHI TÍCH HỢP

## ✅ Đã hoàn thành

### Files mới đã tạo:
1. ✅ `PaymentController.java` - Controller thanh toán cho khách hàng
2. ✅ `RefundManagementController.java` - Controller quản lý hoàn tiền cho admin
3. ✅ `payment-view.fxml` - Giao diện thanh toán
4. ✅ `refund-management-view.fxml` - Giao diện quản lý hoàn tiền

### Files đã cập nhật:
1. ✅ `module-info.java` - Thêm `requires java.desktop;`
2. ✅ `CustomerMainController.java` - Tích hợp payment view
3. ✅ `AdminMainController.java` - Tích hợp refund management
4. ✅ `customer-main.fxml` - Thêm nút thanh toán màu xanh
5. ✅ `admin-main.fxml` - Thêm menu quản lý thanh toán/hoàn tiền

## 🔧 Các Bước Chạy

### Bước 1: Chạy Database Migration
```bash
cd C:\Users\PC\eclipse-workspace\shopgaubong
mysql -u root -p shopgaubong < payment_migration.sql
```

Nếu chưa có database:
```bash
mysql -u root -p < database_setup.sql
mysql -u root -p shopgaubong < payment_migration.sql
```

### Bước 2: Build Project
```bash
mvn clean compile
```

Hoặc trong IDE:
- IntelliJ: `Build > Build Project`
- Eclipse: `Project > Clean and Build`

### Bước 3: Chạy Ứng Dụng
```bash
mvn javafx:run
```

Hoặc chạy trực tiếp:
```bash
java -cp target/classes com.example.shopgaubong.Launcher
```

## 📋 Kiểm Tra Chức Năng

### A. Test với CUSTOMER

1. **Đăng nhập:**
   - Username: `customer` (hoặc tài khoản customer khác)
   - Password: `password`

2. **Thanh toán:**
   - Click nút **"💳 Thanh toán đơn hàng"** (màu xanh)
   - Chọn đơn hàng từ bảng
   - Chọn phương thức thanh toán từ dropdown
   - Xem phí tự động cập nhật
   - Click **"THANH TOÁN NGAY"**
   - Xác nhận thanh toán

3. **Kết quả:**
   - COD/Bank Transfer: Thông báo "Đặt hàng thành công!"
   - Gateway (VNPay/MoMo/SePay): Mở browser với URL thanh toán (mock)

### B. Test với ADMIN

1. **Đăng nhập:**
   - Username: `admin`
   - Password: `password`

2. **Quản lý hoàn tiền:**
   - Click nút **"Quản lý hoàn tiền"** (màu cam)
   - Xem danh sách yêu cầu hoàn tiền đang chờ
   - Click chọn một yêu cầu
   - Xem chi tiết:
     * Lý do khách hàng
     * Thông tin thanh toán
     * Thông tin khách hàng
   - Chọn:
     * **"✓ Duyệt"**: Duyệt hoàn tiền
     * **"✗ Từ chối"**: Nhập lý do và từ chối

3. **Kết quả:**
   - Thông báo thành công
   - Danh sách tự động refresh
   - Yêu cầu đã xử lý biến mất khỏi danh sách chờ

## 🐛 Troubleshooting

### Lỗi: "Cannot find module java.desktop"
**Giải pháp:** Đảm bảo đã cập nhật `module-info.java` với `requires java.desktop;`

### Lỗi: "Cannot resolve symbol OrderStatus.PENDING"
**Giải pháp:** Đã fix trong PaymentController - sử dụng `OrderStatus.PENDING_PAYMENT` và `OrderStatus.PLACED`

### Lỗi: "Table 'shopgaubong.payments' doesn't exist"
**Giải pháp:** Chạy migration script:
```bash
mysql -u root -p shopgaubong < payment_migration.sql
```

### Lỗi: "No orders found"
**Giải pháp:** Tạo đơn hàng test trước:
```sql
INSERT INTO orders (order_number, customer_id, status, subtotal, grand_total, created_by, updated_by)
VALUES ('ORD001', 1, 'PENDING_PAYMENT', 500000, 540000, 'system', 'system');
```

### Ứng dụng không hiển thị giao diện thanh toán
**Kiểm tra:**
1. File `payment-view.fxml` đã được copy vào `src/main/resources/com/example/shopgaubong/`
2. Path trong `loadView()` đúng: `/com/example/shopgaubong/payment-view.fxml`
3. Rebuild project: `mvn clean compile`

## 📝 Dữ Liệu Test

### Tạo đơn hàng test cho customer:
```sql
-- Đơn hàng chờ thanh toán
INSERT INTO orders (order_number, customer_id, status, subtotal, shipping_fee, discount, tax, grand_total, created_by, updated_by)
VALUES 
('ORD001', 2, 'PENDING_PAYMENT', 500000, 30000, 0, 0, 530000, 'system', 'system'),
('ORD002', 2, 'PLACED', 1000000, 50000, 100000, 0, 950000, 'system', 'system');
```

### Tạo thanh toán và yêu cầu hoàn tiền test:
```sql
-- Thanh toán đã hoàn thành
INSERT INTO payments (order_id, method, status, amount, is_paid, transaction_id, paid_at, created_by, updated_by)
VALUES (1, 'VNPAY', 'COMPLETED', 500000, TRUE, 'PAY001', NOW(), 'system', 'system');

-- Yêu cầu hoàn tiền
INSERT INTO refunds (payment_id, refund_number, amount, refund_fee, status, reason, created_by, updated_by)
VALUES (1, 'REF001', 100000, 1000, 'PENDING', 'Sản phẩm bị lỗi', 'customer', 'customer');
```

## 🎯 Kiểm Tra Điểm Chính

### ✅ Customer - Payment View
- [ ] Hiển thị danh sách đơn hàng đúng
- [ ] Chọn đơn hàng → Hiển thị chi tiết
- [ ] Dropdown phương thức thanh toán đầy đủ (5 options)
- [ ] Phí tính tự động khi đổi phương thức
- [ ] COD phí: 2% (min 10k, max 50k)
- [ ] VNPay phí: 2.2%
- [ ] MoMo phí: 2.5%
- [ ] SePay phí: 1.8%
- [ ] Bank Transfer: Miễn phí
- [ ] Nút "THANH TOÁN NGAY" hoạt động
- [ ] Hiển thị thông báo thành công

### ✅ Admin - Refund Management View
- [ ] Hiển thị danh sách yêu cầu hoàn tiền
- [ ] Bảng hiển thị: Mã, Số tiền, Trạng thái, Ngày
- [ ] Trạng thái có màu sắc (PENDING: cam, COMPLETED: xanh, etc.)
- [ ] Click chọn → Hiển thị chi tiết đầy đủ
- [ ] Nút "Duyệt" hoạt động
- [ ] Nút "Từ chối" hoạt động
- [ ] Dialog nhập lý do từ chối
- [ ] Auto-refresh sau thao tác

## 🔍 Log Files

Kiểm tra logs nếu có lỗi:
```
logs/shopgaubong.log
```

## 📞 Support

Nếu gặp vấn đề:
1. Kiểm tra logs
2. Kiểm tra database connection
3. Kiểm tra các file FXML đã được copy đúng vị trí
4. Rebuild project: `mvn clean compile`

---

## ✨ Tính Năng Hoạt Động

✅ **CUSTOMER:**
- Xem danh sách đơn hàng cần thanh toán
- Chọn phương thức thanh toán (COD, Bank Transfer, VNPay, MoMo, SePay)
- Xem chi tiết phí real-time
- Thanh toán đơn hàng

✅ **ADMIN:**
- Xem danh sách yêu cầu hoàn tiền
- Duyệt hoàn tiền
- Từ chối hoàn tiền với lý do
- Xem chi tiết thông tin thanh toán và khách hàng

**Chúc bạn test thành công! 🎉**

