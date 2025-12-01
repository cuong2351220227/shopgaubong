# 🔧 HƯỚNG DẪN FIX LỖI "KHÔNG THỂ MỞ" CHỨC NĂNG THANH TOÁN

## 🐛 Vấn đề

Sau khi đăng nhập và click vào các chức năng thanh toán, hiển thị lỗi **"Không thể mở"**.

---

## ✅ CÁC BƯỚC FIX

### Bước 1: Kiểm tra Database đã chạy migration chưa

```bash
# Kiểm tra bảng payments có cột mới chưa
mysql -u root -p shopgaubong -e "DESCRIBE payments;"

# Kiểm tra bảng refunds tồn tại chưa
mysql -u root -p shopgaubong -e "SHOW TABLES LIKE 'refunds';"
```

**Nếu chưa có, chạy migration:**
```bash
cd C:\Users\PC\eclipse-workspace\shopgaubong
mysql -u root -p shopgaubong < payment_migration.sql
```

### Bước 2: Tạo dữ liệu test

```bash
# Trước tiên, kiểm tra ID của customer trong database
mysql -u root -p shopgaubong -e "SELECT id, username, role FROM accounts WHERE role = 'CUSTOMER';"

# Ghi nhớ customer_id, sau đó sửa file test_data.sql
# Thay customer_id = 2 bằng ID thực tế
# Sau đó chạy:
mysql -u root -p shopgaubong < test_data.sql
```

### Bước 3: Rebuild Project

**Trong IntelliJ IDEA:**
```
Build > Rebuild Project
```

**Trong Eclipse:**
```
Project > Clean...
Project > Build Project
```

**Hoặc dùng Maven (nếu có):**
```bash
mvn clean compile
```

### Bước 4: Kiểm tra File FXML tồn tại

Đảm bảo các file này tồn tại:
```
src/main/resources/com/example/shopgaubong/
  ├── payment-view.fxml ✅
  └── refund-management-view.fxml ✅
```

### Bước 5: Chạy lại ứng dụng

```bash
# Chạy từ IDE hoặc
java -jar target/shopgaubong.jar

# Hoặc
mvn javafx:run
```

---

## 📋 CHECKLIST TROUBLESHOOTING

Khi gặp lỗi, kiểm tra theo thứ tự:

### ✅ 1. Kiểm tra Console Output

Khi click vào chức năng, xem console có in ra:
```
Loading view: /com/example/shopgaubong/payment-view.fxml
View loaded successfully: Thanh toán đơn hàng
```

**Nếu thấy lỗi:**
```
FXML file not found: /com/example/shopgaubong/payment-view.fxml
```
→ File FXML chưa được copy đúng vị trí

### ✅ 2. Kiểm tra Logged In User

Console có in:
```
Customer ID: 2
Total orders found: 3
Pending orders: 2
```

**Nếu thấy:**
```
No logged in user found
```
→ Session không được khởi tạo đúng. Đăng xuất và đăng nhập lại.

### ✅ 3. Kiểm tra Database Connection

Console có lỗi:
```
java.sql.SQLException: Unable to connect to database
```

→ Kiểm tra:
- MySQL đang chạy
- Database `shopgaubong` tồn tại
- File `persistence.xml` có config đúng

### ✅ 4. Kiểm tra Dữ liệu

```sql
-- Kiểm tra có customer không
SELECT * FROM accounts WHERE role = 'CUSTOMER' LIMIT 1;

-- Kiểm tra có đơn hàng không
SELECT * FROM orders WHERE status IN ('PENDING_PAYMENT', 'PLACED') LIMIT 5;

-- Kiểm tra có yêu cầu hoàn tiền không
SELECT * FROM refunds WHERE status = 'PENDING' LIMIT 5;
```

---

## 🔍 CÁC LỖI THƯỜNG GẶP VÀ CÁCH FIX

### ❌ Lỗi 1: "Cannot resolve symbol 'PaymentStatus'"

**Nguyên nhân:** Module chưa compile PaymentStatus.java

**Fix:**
```bash
# Rebuild project
mvn clean compile
# Hoặc trong IDE: Build > Rebuild Project
```

### ❌ Lỗi 2: "Table 'shopgaubong.refunds' doesn't exist"

**Nguyên nhân:** Chưa chạy migration script

**Fix:**
```bash
mysql -u root -p shopgaubong < payment_migration.sql
```

### ❌ Lỗi 3: "Không có đơn hàng cần thanh toán"

**Nguyên nhân:** Database không có dữ liệu test

**Fix:**
```bash
# Sửa customer_id trong test_data.sql trước
mysql -u root -p shopgaubong < test_data.sql
```

### ❌ Lỗi 4: "java.lang.NullPointerException at SessionManager"

**Nguyên nhân:** Chưa đăng nhập hoặc session hết hạn

**Fix:**
- Đăng xuất và đăng nhập lại
- Kiểm tra `SessionManager.getInstance().getCurrentAccount()` không null

### ❌ Lỗi 5: "FXML Load Exception"

**Nguyên nhân:** Lỗi trong file FXML hoặc Controller

**Fix:**
1. Kiểm tra fx:controller trong FXML đúng package
2. Kiểm tra tất cả fx:id trong FXML match với @FXML fields trong Controller
3. Xem stack trace để biết lỗi cụ thể

### ❌ Lỗi 6: "Cannot load VBox"

**Nguyên nhân:** FXML root element không phải VBox

**Fix:** Đảm bảo FXML có root element:
```xml
<VBox xmlns:fx="http://javafx.com/fxml"
      fx:controller="com.example.shopgaubong.controller.PaymentController">
</VBox>
```

---

## 🧪 TEST TỪNG BƯỚC

### Test 1: Customer - Payment View

1. Đăng nhập với customer account
2. Click "💳 Thanh toán đơn hàng"
3. **Kết quả mong đợi:**
   - Hiển thị màn hình thanh toán
   - Có bảng danh sách đơn hàng
   - Có dropdown phương thức thanh toán

**Nếu fail:**
```
Console sẽ in ra lỗi chi tiết
Xem logs/shopgaubong.log
```

### Test 2: Admin - Refund Management

1. Đăng nhập với admin account
2. Click "Quản lý hoàn tiền"
3. **Kết quả mong đợi:**
   - Hiển thị màn hình quản lý hoàn tiền
   - Có bảng danh sách yêu cầu
   - Có nút Duyệt/Từ chối

**Nếu fail:**
```
Console: Error loading view: /com/example/shopgaubong/refund-management-view.fxml
→ Kiểm tra file FXML tồn tại và đúng vị trí
```

---

## 📝 DEBUG CHECKLIST

Trước khi báo lỗi, hãy kiểm tra:

- [ ] MySQL đang chạy
- [ ] Database `shopgaubong` tồn tại
- [ ] Đã chạy `payment_migration.sql`
- [ ] Đã chạy `test_data.sql` (với customer_id đúng)
- [ ] Project đã được rebuild
- [ ] File FXML tồn tại trong `src/main/resources/com/example/shopgaubong/`
- [ ] Đã đăng nhập với đúng role (Customer hoặc Admin)
- [ ] Console có in ra log chi tiết
- [ ] Xem file `logs/shopgaubong.log` nếu có

---

## 🎯 SCRIPT KIỂM TRA NHANH

```sql
-- Chạy script này để kiểm tra toàn bộ
USE shopgaubong;

-- 1. Kiểm tra bảng
SHOW TABLES LIKE 'payments';
SHOW TABLES LIKE 'refunds';

-- 2. Kiểm tra cột mới
SHOW COLUMNS FROM payments LIKE 'status';
SHOW COLUMNS FROM payments LIKE 'refunded_amount';

-- 3. Kiểm tra dữ liệu
SELECT COUNT(*) as customer_count FROM accounts WHERE role = 'CUSTOMER';
SELECT COUNT(*) as pending_orders FROM orders WHERE status IN ('PENDING_PAYMENT', 'PLACED');
SELECT COUNT(*) as pending_refunds FROM refunds WHERE status = 'PENDING';

-- 4. Kiểm tra customer có đơn hàng không
SELECT 
    a.id, 
    a.username, 
    COUNT(o.id) as order_count
FROM accounts a
LEFT JOIN orders o ON a.id = o.customer_id
WHERE a.role = 'CUSTOMER'
GROUP BY a.id;
```

**Kết quả mong đợi:**
```
+-------------------+
| payments          |
+-------------------+
| refunds           |
+-------------------+
customer_count: >= 1
pending_orders: >= 1
pending_refunds: >= 1
```

---

## 🚀 QUICK FIX - TẤT CẢ TRONG MỘT

```bash
# 1. Stop ứng dụng nếu đang chạy

# 2. Chạy tất cả migrations và test data
cd C:\Users\PC\eclipse-workspace\shopgaubong

# Backup database trước (optional)
mysqldump -u root -p shopgaubong > backup_$(date +%Y%m%d).sql

# Chạy migration
mysql -u root -p shopgaubong < payment_migration.sql

# Sửa customer_id trong test_data.sql (dùng text editor)
# Sau đó chạy:
mysql -u root -p shopgaubong < test_data.sql

# 3. Rebuild project
# Trong IDE: Build > Rebuild Project

# 4. Chạy lại ứng dụng
# Run > Run 'Launcher'

# 5. Test
# Login as customer → Click "💳 Thanh toán đơn hàng"
# Login as admin → Click "Quản lý hoàn tiền"
```

---

## ✅ EXPECTED BEHAVIOR

### Customer View thành công:
```
Console output:
> Loading view: /com/example/shopgaubong/payment-view.fxml
> Loading pending orders...
> Customer ID: 2
> Total orders found: 5
> Pending orders: 3
> View loaded successfully: Thanh toán đơn hàng
```

UI hiển thị:
- Bảng có 3 đơn hàng
- Dropdown có 5 phương thức thanh toán
- Các label phí = 0 VND (chưa chọn)

### Admin View thành công:
```
Console output:
> Admin loading view: /com/example/shopgaubong/refund-management-view.fxml
> Loading pending refunds...
> Pending refunds found: 2
> View loaded successfully: Quản lý hoàn tiền
```

UI hiển thị:
- Bảng có 2 yêu cầu hoàn tiền
- Nút Duyệt/Từ chối enabled khi chọn row

---

## 📞 LIÊN HỆ HỖ TRỢ

Nếu vẫn gặp lỗi sau khi làm theo hướng dẫn:

1. Copy toàn bộ **Console output**
2. Copy **Stack trace** nếu có
3. Chụp ảnh màn hình lỗi
4. Báo cáo với thông tin:
   - OS: Windows/Mac/Linux
   - Java version: `java -version`
   - MySQL version: `mysql --version`
   - IDE: IntelliJ/Eclipse/...

**Good luck! 🎉**

