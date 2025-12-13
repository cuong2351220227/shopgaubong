# 🧪 TEST SCRIPT - BÁO CÁO & THỐNG KÊ

## Hướng dẫn test tính năng Báo cáo

### ✅ Checklist kiểm tra:

#### 1. **Truy cập tính năng**
- [ ] Đăng nhập với tài khoản Admin
- [ ] Click vào "Xem báo cáo" trong menu
- [ ] Màn hình báo cáo hiển thị đúng

#### 2. **Kiểm tra thống kê tổng quan**
- [ ] 6 thẻ thống kê hiển thị đầy đủ
- [ ] Doanh thu hôm nay có giá trị (hoặc 0 nếu chưa có đơn)
- [ ] Doanh thu tuần này có giá trị
- [ ] Doanh thu tháng này có giá trị
- [ ] Số đơn hàng hôm nay hiển thị đúng
- [ ] Giá trị TB đơn hàng tính đúng
- [ ] Tỷ lệ hoàn thành hiển thị %

#### 3. **Kiểm tra biểu đồ doanh thu (LineChart)**
- [ ] Biểu đồ hiển thị dữ liệu 30 ngày qua
- [ ] Trục X hiển thị ngày
- [ ] Trục Y hiển thị doanh thu
- [ ] Có thể hover vào điểm để xem tooltip (nếu có)

#### 4. **Kiểm tra biểu đồ trạng thái (PieChart)**
- [ ] Biểu đồ tròn hiển thị phân bố đơn hàng
- [ ] Các trạng thái khác nhau có màu khác nhau
- [ ] Legend hiển thị ở dưới
- [ ] Số lượng đơn hàng hiển thị trong label

#### 5. **Kiểm tra biểu đồ sản phẩm (BarChart)**
- [ ] Hiển thị top 10 sản phẩm bán chạy
- [ ] Cột cao nhất là sản phẩm bán chạy nhất
- [ ] Trục X hiển thị tên sản phẩm
- [ ] Trục Y hiển thị số lượng

#### 6. **Kiểm tra bộ lọc thời gian**
- [ ] ComboBox có 5 tùy chọn: Hôm nay, Tuần này, Tháng này, Năm này, 30 ngày qua
- [ ] Chọn "Hôm nay" → DatePicker tự động cập nhật
- [ ] Chọn "Tuần này" → DatePicker hiển thị từ đầu tuần đến hôm nay
- [ ] Chọn "Tháng này" → DatePicker hiển thị từ đầu tháng
- [ ] Chọn tùy chỉnh từ DatePicker → Có thể chọn ngày bất kỳ
- [ ] Click "Lọc" → Hiển thị alert với kết quả lọc
- [ ] Biểu đồ doanh thu cập nhật theo khoảng thời gian đã chọn

#### 7. **Kiểm tra validation**
- [ ] Không chọn ngày → Click "Lọc" → Hiển thị lỗi "Vui lòng chọn đầy đủ ngày"
- [ ] Ngày bắt đầu > Ngày kết thúc → Click "Lọc" → Hiển thị lỗi

#### 8. **Kiểm tra bảng cảnh báo tồn kho**
- [ ] Hiển thị số lượng sản phẩm cảnh báo trong tiêu đề
- [ ] Bảng có 5 cột: SKU, Tên, Kho, Tồn kho, Điểm đặt lại
- [ ] Cột "Tồn kho" hiển thị màu đỏ
- [ ] Nếu không có sản phẩm → Hiển thị "✅ Không có sản phẩm nào tồn kho thấp"

#### 9. **Kiểm tra nút Làm mới**
- [ ] Click "Làm mới" → Tất cả dữ liệu được cập nhật
- [ ] Hiển thị thông báo "Đã làm mới dữ liệu báo cáo"

#### 10. **Kiểm tra giao diện**
- [ ] Layout đẹp, responsive
- [ ] Thẻ thống kê có shadow và hover effect
- [ ] Nút có màu sắc phù hợp (Làm mới: xanh lá, Lọc: xanh)
- [ ] ScrollPane hoạt động khi nội dung dài
- [ ] Font chữ rõ ràng, dễ đọc

---

## 🧪 Test Cases

### Test Case 1: Xem báo cáo với dữ liệu đầy đủ
**Điều kiện:** Database có đơn hàng, sản phẩm, tồn kho

**Steps:**
1. Đăng nhập Admin
2. Click "Xem báo cáo"
3. Kiểm tra tất cả thống kê hiển thị
4. Kiểm tra tất cả biểu đồ có dữ liệu

**Kết quả mong đợi:**
- Tất cả số liệu hiển thị chính xác
- Biểu đồ có dữ liệu và màu sắc

---

### Test Case 2: Xem báo cáo với database trống
**Điều kiện:** Database không có đơn hàng nào

**Steps:**
1. Đăng nhập Admin
2. Click "Xem báo cáo"
3. Kiểm tra thống kê

**Kết quả mong đợi:**
- Doanh thu hiển thị "0 ₫"
- Đơn hàng hiển thị "0"
- Biểu đồ trống (không có data points)
- Không bị crash

---

### Test Case 3: Lọc theo "Hôm nay"
**Steps:**
1. Vào báo cáo
2. Chọn "Hôm nay" từ ComboBox
3. Click "Lọc"

**Kết quả mong đợi:**
- DatePicker hiển thị ngày hôm nay cho cả start và end
- Alert hiển thị kết quả lọc với doanh thu và đơn hàng hôm nay
- Biểu đồ doanh thu chỉ hiển thị 1 điểm (ngày hôm nay)

---

### Test Case 4: Lọc với ngày bắt đầu > ngày kết thúc
**Steps:**
1. Vào báo cáo
2. Chọn Start Date = 15/12/2025
3. Chọn End Date = 10/12/2025
4. Click "Lọc"

**Kết quả mong đợi:**
- Hiển thị alert lỗi "Ngày bắt đầu không thể sau ngày kết thúc"
- Không thực hiện lọc

---

### Test Case 5: Kiểm tra cảnh báo tồn kho thấp
**Điều kiện:** Có sản phẩm với quantity < reorderPoint

**Steps:**
1. Vào báo cáo
2. Cuộn xuống phần "CẢNH BÁO TỒN KHO THẤP"

**Kết quả mong đợi:**
- Bảng hiển thị sản phẩm có quantity < reorderPoint
- Số lượng hiển thị màu đỏ
- Tiêu đề hiển thị số lượng sản phẩm cảnh báo

---

### Test Case 6: Top sản phẩm bán chạy
**Điều kiện:** Có đơn hàng hoàn thành

**Steps:**
1. Vào báo cáo
2. Xem biểu đồ "TOP 10 SẢN PHẨM BÁN CHẠY"

**Kết quả mong đợi:**
- Hiển thị tối đa 10 sản phẩm
- Sản phẩm bán nhiều nhất có cột cao nhất
- Sắp xếp từ cao đến thấp

---

### Test Case 7: Làm mới dữ liệu
**Steps:**
1. Vào báo cáo
2. Thay đổi dữ liệu trong database (thêm đơn hàng mới)
3. Click "Làm mới"

**Kết quả mong đợi:**
- Tất cả thống kê được cập nhật với dữ liệu mới
- Biểu đồ cập nhật
- Hiển thị thông báo "Đã làm mới dữ liệu báo cáo"

---

## 🐛 Các lỗi có thể gặp

### Lỗi 1: Không tải được report-view.fxml
**Triệu chứng:** Alert "Không tìm thấy file FXML"

**Giải pháp:**
- Kiểm tra file `report-view.fxml` có trong `src/main/resources/com/example/shopgaubong/`
- Build lại project
- Clean & rebuild

### Lỗi 2: Charts không hiển thị dữ liệu
**Triệu chứng:** Biểu đồ trống dù có dữ liệu

**Giải pháp:**
- Kiểm tra database có đơn hàng với status COMPLETED
- Kiểm tra console log xem có exception không
- Kiểm tra ReportService methods

### Lỗi 3: CSS không áp dụng
**Triệu chứng:** Giao diện không đẹp như mong đợi

**Giải pháp:**
- Kiểm tra file `report-styles.css` có trong resources
- Kiểm tra FXML có tag `<stylesheets>` với đường dẫn đúng
- Build lại project

### Lỗi 4: DatePicker không hoạt động
**Triệu chứng:** Không chọn được ngày

**Giải pháp:**
- Kiểm tra JavaFX version
- Kiểm tra fx:id trong FXML khớp với @FXML trong controller

---

## 📊 Dữ liệu test

Để test đầy đủ, cần có:

### Dữ liệu tối thiểu:
- ✅ Ít nhất 5 đơn hàng với status COMPLETED
- ✅ Đơn hàng ở các ngày khác nhau (trong 30 ngày qua)
- ✅ Ít nhất 5 sản phẩm khác nhau
- ✅ Ít nhất 1 sản phẩm có tồn kho thấp

### Tạo dữ liệu test:
```sql
-- Tạo đơn hàng test với nhiều ngày khác nhau
-- Đơn hàng hôm nay
INSERT INTO orders (...) VALUES (...);

-- Đơn hàng 7 ngày trước
INSERT INTO orders (...) VALUES (...);

-- Đơn hàng 15 ngày trước
INSERT INTO orders (...) VALUES (...);

-- Tạo sản phẩm tồn kho thấp
UPDATE stock_items 
SET quantity = 5, reorder_point = 10 
WHERE item_id = 1;
```

---

## ✅ Kết luận test

Sau khi test xong, đảm bảo:

- [ ] Tất cả test cases pass
- [ ] Không có lỗi trong console
- [ ] UI/UX mượt mà, không lag
- [ ] Dữ liệu hiển thị chính xác
- [ ] Charts hoạt động tốt
- [ ] Bộ lọc hoạt động đúng
- [ ] Validation hoạt động

---

**Nếu tất cả pass → Tính năng sẵn sàng production! 🎉**
