# 🧪 HƯỚNG DẪN KIỂM TRA DỰ ÁN

## ⚡ QUICK START

### 1. Khởi động ứng dụng
```bash
# Trong IntelliJ IDEA hoặc Eclipse:
# Run class: com.example.shopgaubong.Launcher
```

### 2. Đăng nhập

#### Admin Account:
- **Username:** admin
- **Password:** (như đã setup trong database)

#### Customer Account:
- **Username:** customer
- **Password:** (như đã setup trong database)

---

## ✅ TEST SCENARIOS

### SCENARIO 1: Admin - Quản lý Danh mục ✅

**Mục tiêu:** Tạo và quản lý cây danh mục sản phẩm

**Steps:**
1. Login với tài khoản Admin
2. Click **"Quản lý danh mục"** ở menu trái
3. Click **"➕ Thêm mới"**
4. Nhập:
   - Tên: `Gấu bông`
   - Mô tả: `Danh mục gấu bông các loại`
   - Danh mục cha: `(để trống - tạo danh mục gốc)`
   - Trạng thái: ✓ Đang hoạt động
5. Click **"💾 Lưu"**
6. **Verify:** Thông báo "Thêm danh mục thành công!"
7. **Verify:** "Gấu bông" xuất hiện trong bảng

**Tiếp tục - Tạo danh mục con:**
8. Click **"➕ Thêm mới"**
9. Nhập:
   - Tên: `Gấu Teddy`
   - Mô tả: `Gấu Teddy Bear truyền thống`
   - Danh mục cha: `Gấu bông` ← **Chọn từ dropdown**
   - Trạng thái: ✓ Đang hoạt động
10. Click **"💾 Lưu"**
11. **Verify:** "Gấu Teddy" xuất hiện trong bảng

**Test Search:**
12. Nhập "Teddy" vào ô tìm kiếm
13. **Verify:** Chỉ hiển thị "Gấu Teddy"
14. Xóa search → **Verify:** Hiển thị tất cả

**Test Edit:**
15. Click vào dòng "Gấu Teddy" trong bảng
16. **Verify:** Thông tin hiện trong form bên phải
17. Sửa mô tả thành: `Gấu Teddy Bear cổ điển`
18. Click **"💾 Lưu"**
19. **Verify:** Mô tả đã cập nhật trong bảng

**Test Delete:**
20. Tạo thêm danh mục test: "Test Delete"
21. Click chọn "Test Delete" trong bảng
22. Click **"🗑️ Xóa"**
23. Click **OK** trong dialog xác nhận
24. **Verify:** "Test Delete" biến mất khỏi bảng

---

### SCENARIO 2: Admin - Quản lý Sản phẩm ✅

**Mục tiêu:** Tạo và quản lý sản phẩm

**Prerequisites:** Đã có danh mục "Gấu Teddy" từ Scenario 1

**Steps:**
1. Vẫn ở trang Admin Main
2. Click **"Quản lý sản phẩm"**
3. Click **"➕ Thêm mới"**
4. Nhập thông tin:
   ```
   Mã SKU: TB-001
   Tên SP: Gấu Teddy Brown
   Danh mục: Gấu Teddy (chọn từ dropdown)
   Giá: 250000
   Đơn vị: Cái
   Khối lượng: 0.5
   Hình ảnh: https://example.com/teddy-brown.jpg
   Mô tả: Gấu Teddy màu nâu, cao 30cm, chất liệu nhung mềm
   Trạng thái: ✓ Đang hoạt động
   ```
5. Click **"💾 Lưu"**
6. **Verify:** Thông báo "Thêm sản phẩm thành công!"
7. **Verify:** "TB-001 - Gấu Teddy Brown" xuất hiện trong bảng
8. **Verify:** Giá hiển thị: "250,000 đ"

**Test Validation - SKU Unique:**
9. Click **"➕ Thêm mới"** lại
10. Nhập:
    ```
    Mã SKU: TB-001  ← Trùng với sản phẩm đã tạo
    Tên SP: Test Duplicate
    Danh mục: Gấu Teddy
    Giá: 100000
    ```
11. Click **"💾 Lưu"**
12. **Verify:** Thông báo lỗi "Mã SKU đã tồn tại: TB-001"

**Test Validation - Price > 0:**
13. Click **"➕ Thêm mới"**
14. Nhập:
    ```
    Mã SKU: TB-002
    Tên SP: Test Price
    Danh mục: Gấu Teddy
    Giá: 0  ← Không hợp lệ
    ```
15. Click **"💾 Lưu"**
16. **Verify:** Thông báo cảnh báo "Giá phải lớn hơn 0!"

**Test Edit Product:**
17. Click vào dòng "TB-001" trong bảng
18. **Verify:** Form hiển thị đầy đủ thông tin
19. Sửa giá thành: `300000`
20. Click **"💾 Lưu"**
21. **Verify:** Giá cập nhật thành "300,000 đ"

**Test Search:**
22. Nhập "TB-001" vào ô tìm kiếm
23. **Verify:** Chỉ hiển thị sản phẩm TB-001
24. Xóa và nhập "Brown"
25. **Verify:** Hiển thị "Gấu Teddy Brown"

**Tạo thêm sản phẩm để test sau:**
26. Tạo thêm 2-3 sản phẩm nữa:
    ```
    SKU: TB-002, Tên: Gấu Teddy Pink, Giá: 280000
    SKU: PD-001, Tên: Gấu Panda, Giá: 320000
    SKU: RB-001, Tên: Thỏ bông trắng, Giá: 150000
    ```

---

### SCENARIO 3: Customer - Xem và Mua Sản phẩm ✅

**Mục tiêu:** Khách hàng xem sản phẩm và thêm vào giỏ hàng

**Prerequisites:** 
- Đã có danh mục và sản phẩm từ Scenario 1 & 2
- Logout khỏi Admin account

**Steps:**
1. **Logout:** Click "Đăng xuất" ở góc phải trên
2. **Login Customer:**
   - Username: `customer`
   - Password: (password của customer)
3. Click **"Xem sản phẩm"** trong menu trái Customer
4. **Verify:** Hiển thị danh sách sản phẩm đang hoạt động

**Test Filter by Category:**
5. Click dropdown "Chọn danh mục"
6. Chọn **"Gấu Teddy"**
7. **Verify:** Chỉ hiển thị các sản phẩm thuộc danh mục "Gấu Teddy"
8. Chọn **"-- Tất cả danh mục --"**
9. **Verify:** Hiển thị tất cả sản phẩm

**Test Search:**
10. Nhập "Teddy" vào ô tìm kiếm
11. **Verify:** Chỉ hiển thị các sản phẩm có "Teddy" trong tên hoặc SKU

**Test Product Details:**
12. Click vào dòng "TB-001 - Gấu Teddy Brown"
13. **Verify bên phải:**
    - Tên: "Gấu Teddy Brown"
    - SKU: "SKU: TB-001"
    - Giá: "300,000 đ / Cái" (hoặc giá bạn đã set)
    - Danh mục: "Danh mục: Gấu Teddy"
    - Mô tả hiển thị trong TextArea

**Test Add to Cart:**
14. Với sản phẩm TB-001 đã chọn
15. Spinner số lượng: Đặt = **2**
16. Click **"🛒 THÊM VÀO GIỎ HÀNG"**
17. **Verify:** Thông báo "Đã thêm vào giỏ hàng: Gấu Teddy Brown (x2)"

**Test Add Same Product Again:**
18. Giữ nguyên sản phẩm TB-001
19. Đặt số lượng = **1**
20. Click **"🛒 THÊM VÀO GIỎ HÀNG"** lại
21. **Verify:** Thông báo "Sản phẩm 'Gấu Teddy Brown' đã có trong giỏ hàng! Vui lòng vào 'Giỏ hàng' để cập nhật số lượng."

**Test Add Different Product:**
22. Click vào sản phẩm khác (VD: "PD-001 - Gấu Panda")
23. Đặt số lượng = **1**
24. Click **"🛒 THÊM VÀO GIỎ HÀNG"**
25. **Verify:** Thông báo "Đã thêm vào giỏ hàng: Gấu Panda (x1)"

---

### SCENARIO 4: Customer - Thanh toán (Payment) ✅

**Mục tiêu:** Thanh toán cho các đơn hàng đã đặt

**Prerequisites:** Cần có đơn hàng với status PENDING hoặc CONFIRMED

**Note:** Vì chưa có Checkout flow hoàn chỉnh, bạn cần tạo test data trong database:
```sql
-- Update cart to PENDING status for testing
UPDATE orders 
SET status = 'PENDING' 
WHERE customer_id = (SELECT id FROM accounts WHERE username = 'customer')
AND status = 'CART';
```

**Steps:**
1. Vẫn ở trang Customer Main
2. Click **"💳 Thanh toán đơn hàng"**
3. **Verify:** Hiển thị danh sách đơn hàng cần thanh toán
4. Click chọn đơn hàng trong bảng
5. **Verify bên phải:**
   - Thông tin đơn hàng: Số đơn, Ngày đặt, Tổng tiền
   - Danh sách sản phẩm trong đơn
   - Các phương thức thanh toán
6. Chọn phương thức: **"VNPay"**
7. **Verify:** Phí thanh toán hiển thị (2.2% của tổng)
8. **Verify:** Tổng thanh toán = Tổng đơn hàng + Phí
9. Click **"THANH TOÁN NGAY"**
10. Click **OK** trong dialog xác nhận
11. **Verify:** Browser mở URL VNPay (hoặc thông báo thành công nếu test mode)

**Test Different Payment Methods:**
12. Chọn đơn khác (hoặc tạo mới)
13. Thử các phương thức:
    - **COD:** Phí 2% (min 10k, max 50k)
    - **Chuyển khoản:** Miễn phí (0%)
    - **MoMo:** Phí 2.5%
    - **SePay:** Phí 1.8%
14. **Verify:** Mỗi phương thức tính phí đúng

---

### SCENARIO 5: Admin - Quản lý Hoàn tiền ✅

**Mục tiêu:** Duyệt hoặc từ chối yêu cầu hoàn tiền

**Prerequisites:** Cần có refund request trong database

**Test Data:**
```sql
-- Tạo refund request test
INSERT INTO refunds (payment_id, reason, amount, status, created_at, created_by) 
VALUES (
  (SELECT id FROM payments ORDER BY id DESC LIMIT 1),
  'Sản phẩm bị lỗi',
  300000,
  'PENDING',
  NOW(),
  'customer'
);
```

**Steps:**
1. Logout customer, Login lại với **Admin**
2. Click **"Quản lý hoàn tiền"** (nút màu cam)
3. **Verify:** Hiển thị danh sách yêu cầu hoàn tiền đang chờ
4. Click chọn yêu cầu trong bảng
5. **Verify bên phải:**
   - Lý do khách hàng
   - Thông tin thanh toán gốc
   - Số tiền hoàn
   - Thông tin khách hàng
6. Click **"✓ Duyệt"**
7. Click **OK** trong dialog xác nhận
8. **Verify:** 
   - Thông báo "Đã duyệt yêu cầu hoàn tiền!"
   - Yêu cầu biến mất khỏi danh sách (hoặc status = COMPLETED)

**Test Reject:**
9. Tạo refund request mới (hoặc select existing)
10. Click **"✗ Từ chối"**
11. **Verify:** Dialog xuất hiện yêu cầu nhập lý do
12. Nhập lý do: `Không đủ điều kiện hoàn tiền`
13. Click **OK**
14. **Verify:**
    - Thông báo "Đã từ chối yêu cầu hoàn tiền!"
    - Status = REJECTED

---

## 🎯 TEST MATRIX

| Feature | Test Case | Status |
|---------|-----------|--------|
| Category Management | Create root category | ✅ |
| Category Management | Create child category | ✅ |
| Category Management | Edit category | ✅ |
| Category Management | Delete category | ✅ |
| Category Management | Search category | ✅ |
| Item Management | Create item | ✅ |
| Item Management | Edit item | ✅ |
| Item Management | Delete item | ✅ |
| Item Management | Search item | ✅ |
| Item Management | Validation (SKU unique) | ✅ |
| Item Management | Validation (Price > 0) | ✅ |
| Product Catalog | View products | ✅ |
| Product Catalog | Filter by category | ✅ |
| Product Catalog | Search products | ✅ |
| Product Catalog | View product details | ✅ |
| Product Catalog | Add to cart (new) | ✅ |
| Product Catalog | Add to cart (existing) | ✅ |
| Payment | View pending orders | ✅ |
| Payment | Select payment method | ✅ |
| Payment | Calculate fees | ✅ |
| Payment | Process payment | ✅ |
| Refund Management | View pending refunds | ✅ |
| Refund Management | Approve refund | ✅ |
| Refund Management | Reject refund | ✅ |

---

## 🐛 COMMON ISSUES & SOLUTIONS

### Issue 1: "Không tìm thấy file FXML"
**Solution:** Kiểm tra path trong controller method `loadView()`:
- Phải là: `/com/example/shopgaubong/xxx-view.fxml`
- FXML files phải ở: `src/main/resources/com/example/shopgaubong/`

### Issue 2: "Private field never assigned" warnings
**Solution:** Đây là warnings của IDE, không ảnh hưởng. JavaFX tự động inject `@FXML` fields khi load FXML.

### Issue 3: "EntityManager is closed"
**Solution:** 
- Kiểm tra Hibernate configuration
- Verify database connection
- Check `persistence.xml`

### Issue 4: Không thêm được vào giỏ hàng
**Solution:**
- Verify warehouse tồn tại (ID = 1)
- Check stock availability
- Xem console logs để debug

### Issue 5: Current user is null
**Solution:**
- Verify login thành công
- Check SessionManager có lưu username
- Restart application

---

## ✅ CHECKLIST HOÀN THÀNH

Sau khi test xong, verify các items sau:

- [ ] Admin có thể tạo, sửa, xóa danh mục
- [ ] Admin có thể tạo, sửa, xóa sản phẩm
- [ ] Validation hoạt động (SKU unique, price > 0)
- [ ] Customer có thể xem danh sách sản phẩm
- [ ] Customer có thể lọc theo danh mục
- [ ] Customer có thể tìm kiếm sản phẩm
- [ ] Customer có thể thêm sản phẩm vào giỏ
- [ ] Customer có thể thanh toán đơn hàng
- [ ] Phí thanh toán tính đúng cho từng phương thức
- [ ] Admin có thể duyệt/từ chối hoàn tiền
- [ ] Tất cả dialogs (success, error, warning) hiển thị đúng
- [ ] UI responsive, không bị lag
- [ ] Không có exceptions trong console (ngoài warnings)

---

**Tác giả:** GitHub Copilot  
**Version:** 1.0  
**Ngày:** 30/11/2025

