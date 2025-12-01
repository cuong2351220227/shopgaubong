# 🎯 HƯỚNG DẪN SỬ DỤNG DỮ LIỆU MẪU

## 📦 File: `sample_data_for_payment.sql`

---

## 🚀 CÁCH CHẠY NHANH

### Bước 1: Chạy script tạo dữ liệu
```bash
cd C:\Users\PC\eclipse-workspace\shopgaubong
mysql -u root -p shopgaubong < sample_data_for_payment.sql
```

### Bước 2: Chạy ứng dụng
```bash
# Trong IDE: Run > Run 'Launcher'
```

### Bước 3: Test ngay!
- **Customer:** Login → Click "💳 Thanh toán đơn hàng"
- **Admin:** Login → Click "Quản lý hoàn tiền"

---

## 📊 DỮ LIỆU ĐÃ TẠO

### 👤 Tài khoản
- **Username:** `customer_test`
- **Password:** (dùng password mặc định của hệ thống)
- **Role:** CUSTOMER
- Nếu không có, script sẽ tự động dùng customer có sẵn

### 📦 Đơn hàng cần thanh toán (4 đơn)

| Mã đơn | Trạng thái | Tiền hàng | Tổng cộng | Mục đích test |
|--------|------------|-----------|-----------|---------------|
| **TEST001** | PENDING_PAYMENT | 200,000đ | 220,000đ | Test phí COD tối thiểu (10,000đ) |
| **TEST002** | PLACED | 500,000đ | 480,000đ | Test VNPay/MoMo/SePay |
| **TEST003** | PENDING_PAYMENT | 3,000,000đ | 2,850,000đ | Test phí COD tối đa (50,000đ) |
| **TEST004** | PENDING_PAYMENT | 800,000đ | 840,000đ | Test chuyển khoản (miễn phí) |

### 💰 Thanh toán đã hoàn thành (2 đơn)

| Mã đơn | Phương thức | Số tiền | Trạng thái | Mục đích |
|--------|-------------|---------|-----------|----------|
| **TEST005** | VNPAY | 950,000đ | COMPLETED | Có 2 yêu cầu hoàn tiền |
| **TEST006** | MOMO | 630,000đ | COMPLETED | Có 1 yêu cầu hoàn tiền |

### 🔄 Yêu cầu hoàn tiền (3 yêu cầu)

| Mã hoàn | Số tiền | Trạng thái | Lý do |
|---------|---------|-----------|-------|
| **TESTREF001** | 200,000đ | PENDING | Sản phẩm bị lỗi |
| **TESTREF002** | 50,000đ | PENDING | Giao hàng chậm |
| **TESTREF003** | 300,000đ | PENDING | Đổi sản phẩm |

---

## 🎮 TEST CASES

### Test Case 1: COD - Phí tối thiểu
```
Đơn hàng: TEST001
Tiền hàng: 200,000đ
Chọn: COD
Kết quả mong đợi:
  - Phí COD: 10,000đ (200k × 2% = 4k < min 10k)
  - Tổng: 230,000đ
```

### Test Case 2: VNPay Gateway
```
Đơn hàng: TEST002
Tiền hàng: 450,000đ (đã giảm 50k)
Chọn: VNPay
Kết quả mong đợi:
  - Phí Gateway: 9,900đ (450k × 2.2%)
  - Tổng: 489,900đ
```

### Test Case 3: MoMo Wallet
```
Đơn hàng: TEST002
Tiền hàng: 450,000đ
Chọn: MoMo
Kết quả mong đợi:
  - Phí Gateway: 11,250đ (450k × 2.5%)
  - Tổng: 491,250đ
```

### Test Case 4: SePay - Phí thấp nhất
```
Đơn hàng: TEST002
Tiền hàng: 450,000đ
Chọn: SePay
Kết quả mong đợi:
  - Phí Gateway: 8,100đ (450k × 1.8%)
  - Tổng: 488,100đ
```

### Test Case 5: COD - Phí tối đa
```
Đơn hàng: TEST003
Tiền hàng: 2,800,000đ (đã giảm 200k)
Chọn: COD
Kết quả mong đợi:
  - Phí COD: 50,000đ (2.8M × 2% = 56k > max 50k)
  - Tổng: 2,900,000đ
```

### Test Case 6: Chuyển khoản - Miễn phí
```
Đơn hàng: TEST004
Tiền hàng: 800,000đ
Chọn: BANK_TRANSFER
Kết quả mong đợi:
  - Phí Gateway: 0đ
  - Phí COD: 0đ
  - Tổng: 840,000đ
```

### Test Case 7: Duyệt hoàn tiền
```
Admin:
1. Click "Quản lý hoàn tiền"
2. Chọn TESTREF001 (200,000đ - Sản phẩm lỗi)
3. Xem chi tiết:
   - Lý do: "Sản phẩm bị lỗi khi nhận hàng..."
   - Thanh toán gốc: 950,000đ qua VNPay
   - Khách hàng: Nguyễn Văn Test
4. Click "✓ Duyệt"
5. Kết quả: Hoàn tiền thành công
```

### Test Case 8: Từ chối hoàn tiền
```
Admin:
1. Chọn TESTREF002 (50,000đ - Giao hàng chậm)
2. Click "✗ Từ chối"
3. Nhập lý do: "Giao hàng chậm do lỗi đơn vị vận chuyển, không phải lỗi shop"
4. Kết quả: Yêu cầu bị từ chối với lý do rõ ràng
```

---

## 📋 CHECKLIST TRƯỚC KHI TEST

- [ ] ✅ Đã chạy `payment_migration.sql`
- [ ] ✅ Đã chạy `sample_data_for_payment.sql`
- [ ] ✅ Project đã rebuild
- [ ] ✅ MySQL đang chạy
- [ ] ✅ Ứng dụng đã khởi động

---

## 🔍 VERIFY DỮ LIỆU

### Kiểm tra nhanh trong MySQL:
```sql
USE shopgaubong;

-- Kiểm tra đơn hàng
SELECT order_number, status, grand_total 
FROM orders 
WHERE order_number LIKE 'TEST%';

-- Kiểm tra thanh toán
SELECT transaction_id, method, status, amount
FROM payments
WHERE transaction_id LIKE 'TEST%';

-- Kiểm tra hoàn tiền
SELECT refund_number, amount, status
FROM refunds
WHERE refund_number LIKE 'TEST%';
```

**Kết quả mong đợi:**
- 6 đơn hàng (4 chờ thanh toán + 2 đã thanh toán)
- 2 thanh toán đã hoàn thành
- 3 yêu cầu hoàn tiền đang chờ

---

## 🎯 LUỒNG TEST ĐẦY ĐỦ

### 1️⃣ Test Customer - Thanh toán

```
1. Mở ứng dụng
2. Login với customer_test (hoặc customer có sẵn)
3. Click "💳 Thanh toán đơn hàng" (nút xanh)
4. Xem bảng → Có 4 đơn hàng test
5. Click chọn TEST001
6. Xem chi tiết đơn hàng hiển thị
7. Chọn phương thức: COD
8. Xem phí:
   ✓ Tiền hàng: 200,000 VND
   ✓ Phí COD: 10,000 VND
   ✓ Phí vận chuyển: 20,000 VND
   ✓ TỔNG CỘNG: 230,000 VND
9. Thử đổi sang VNPay → Phí thay đổi thành 2.2%
10. Click "THANH TOÁN NGAY"
11. Xác nhận → Thành công!
```

### 2️⃣ Test Admin - Quản lý hoàn tiền

```
1. Logout customer
2. Login với admin
3. Click "Quản lý hoàn tiền" (nút cam)
4. Xem bảng → Có 3 yêu cầu hoàn tiền
5. Click chọn TESTREF001
6. Xem chi tiết:
   ✓ Lý do khách hàng
   ✓ Thông tin thanh toán gốc
   ✓ Thông tin khách hàng
   ✓ Số tiền hoàn: 200,000 VND
   ✓ Phí hoàn: 2,000 VND
   ✓ Thực nhận: 198,000 VND
7. Click "✓ Duyệt"
8. Xác nhận → Duyệt thành công!
9. Bảng tự động refresh
10. Yêu cầu đã duyệt biến mất khỏi danh sách chờ
```

---

## 🧪 KẾT QUẢ MONG ĐỢI

### Console Output khi thành công:

**Customer View:**
```
Loading view: /com/example/shopgaubong/payment-view.fxml
Loading pending orders...
Customer ID: [ID]
Total orders found: 6
Pending orders: 4
View loaded successfully: Thanh toán đơn hàng
```

**Admin View:**
```
Admin loading view: /com/example/shopgaubong/refund-management-view.fxml
Loading pending refunds...
Pending refunds found: 3
View loaded successfully: Quản lý hoàn tiền
```

---

## 🗑️ XÓA DỮ LIỆU TEST

Khi test xong, chạy các lệnh sau để xóa dữ liệu test:

```sql
USE shopgaubong;

DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE order_number LIKE 'TEST%');
DELETE FROM refunds WHERE refund_number LIKE 'TEST%';
DELETE FROM payments WHERE transaction_id LIKE 'TEST%';
DELETE FROM orders WHERE order_number LIKE 'TEST%';
DELETE FROM account_profiles WHERE account_id = (SELECT id FROM accounts WHERE username = 'customer_test');
DELETE FROM accounts WHERE username = 'customer_test';

SELECT '✓ Dữ liệu test đã xóa!' as Status;
```

---

## 💡 TIPS

1. **Nếu không thấy đơn hàng:**
   - Kiểm tra đã login đúng customer chưa
   - Check console log xem customer_id
   - Verify database có dữ liệu: `SELECT * FROM orders WHERE order_number LIKE 'TEST%';`

2. **Nếu phí tính sai:**
   - Kiểm tra PaymentMethod enum có đúng fee rates không
   - Check console log để debug

3. **Nếu không hoàn tiền được:**
   - Kiểm tra payment có status COMPLETED không
   - Verify refund có status PENDING không

4. **Test nhiều lần:**
   - Có thể xóa và tạo lại dữ liệu
   - Hoặc tạo thêm đơn hàng mới với prefix khác

---

## ✅ CHECKLIST TESTING

### Customer - Payment
- [ ] Hiển thị 4 đơn hàng test
- [ ] Click chọn đơn → Hiển thị chi tiết
- [ ] Dropdown có 5 phương thức
- [ ] Phí COD tối thiểu: 10,000đ (TEST001)
- [ ] Phí COD tối đa: 50,000đ (TEST003)
- [ ] VNPay phí: 2.2%
- [ ] MoMo phí: 2.5%
- [ ] SePay phí: 1.8%
- [ ] Bank Transfer: Miễn phí
- [ ] Thanh toán thành công

### Admin - Refund
- [ ] Hiển thị 3 yêu cầu hoàn tiền
- [ ] Click chọn → Hiển thị chi tiết đầy đủ
- [ ] Duyệt hoàn tiền thành công
- [ ] Từ chối với lý do
- [ ] Auto-refresh sau thao tác
- [ ] Màu sắc trạng thái đúng

---

## 🎉 HOÀN TẤT!

Bây giờ bạn có đầy đủ dữ liệu để test toàn bộ chức năng thanh toán!

**Chúc bạn test thành công! 🚀**

