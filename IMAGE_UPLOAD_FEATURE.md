# 📸 TÍNH NĂNG UPLOAD ẢNH SẢN PHẨM - HOÀN THÀNH

## 🎉 Đã thay đổi từ URL sang Upload ảnh từ máy!

### ✅ Các thay đổi đã thực hiện:

#### 1. **ImageUtil.java** - Utility class xử lý ảnh ✅
**Vị trí:** `src/main/java/com/example/shopgaubong/util/ImageUtil.java`

**Các phương thức:**
- `fileToBase64()` - Convert File ảnh thành Base64 string
- `base64ToImage()` - Convert Base64 string thành JavaFX Image
- `resizeAndConvertToBase64()` - Resize ảnh và convert (tiết kiệm dung lượng)
- `isValidImageFile()` - Kiểm tra file có phải ảnh hợp lệ
- `getFileSizeMB()` - Lấy kích thước file theo MB
- `loadImageToView()` - Load ảnh Base64 vào ImageView

**Tính năng:**
- Hỗ trợ: JPG, JPEG, PNG, GIF, BMP
- Tự động resize ảnh xuống max 800x800px (giữ tỷ lệ)
- Kiểm tra kích thước file (max 5MB)
- Convert ảnh sang Base64 để lưu vào database

---

#### 2. **Item.java** - Entity ✅
**Thay đổi:**
```java
// CŨ:
@Column(length = 500)
private String imageUrl;

// MỚI:
@Lob
@Column(columnDefinition = "LONGTEXT")
private String imageData; // Lưu ảnh Base64
```

**Lý do:**
- LONGTEXT hỗ trợ lưu dữ liệu lớn (ảnh Base64)
- Không phụ thuộc vào server lưu trữ ảnh bên ngoài
- Ảnh được lưu trực tiếp trong database

---

#### 3. **ItemService.java** - Service Layer ✅
**Thay đổi:**
- `createItem()` - Parameter `imageUrl` → `imageData`
- `updateItem()` - Parameter `imageUrl` → `imageData`
- Tất cả logic xử lý đã được cập nhật

---

#### 4. **item-management-view.fxml** - Giao diện ✅
**Thay đổi:**
```xml
<!-- CŨ: TextField nhập URL -->
<TextField fx:id="txtImageUrl" promptText="URL hình ảnh"/>

<!-- MỚI: Button chọn ảnh + Preview -->
<VBox spacing="5">
    <HBox spacing="10">
        <Button fx:id="btnChooseImage" text="Chọn ảnh"/>
        <Label fx:id="lblImageFileName" text="Chưa chọn ảnh"/>
    </HBox>
    <ImageView fx:id="imgPreview" fitWidth="150" fitHeight="150"/>
</VBox>
```

**Tính năng mới:**
- Nút "Chọn ảnh" để mở FileChooser
- Label hiển thị tên file và kích thước
- ImageView preview ảnh đã chọn (150x150px)

---

#### 5. **ItemManagementController.java** - Controller ✅
**Thay đổi:**

**Fields mới:**
```java
@FXML private Button btnChooseImage;
@FXML private Label lblImageFileName;
@FXML private ImageView imgPreview;
private String currentImageData = null; // Lưu ảnh Base64 tạm
```

**Method mới:**
```java
@FXML
private void handleChooseImage() {
    // Mở FileChooser
    // Validate file (phải là ảnh, max 5MB)
    // Resize và convert sang Base64
    // Preview ảnh
}
```

**Logic:**
1. User click "Chọn ảnh"
2. Mở FileChooser (filter chỉ ảnh)
3. Validate file size (max 5MB)
4. Resize ảnh xuống 800x800px
5. Convert sang Base64
6. Lưu vào `currentImageData`
7. Preview trong ImageView
8. Khi Save → lưu Base64 vào database

---

#### 6. **image_migration.sql** - Database Migration ✅
**Script SQL:**
```sql
-- Thêm cột mới
ALTER TABLE items 
ADD COLUMN image_data LONGTEXT AFTER image_url;

-- Copy dữ liệu cũ (nếu có)
UPDATE items SET image_data = image_url 
WHERE image_url IS NOT NULL;

-- Xóa cột cũ
ALTER TABLE items DROP COLUMN image_url;
```

**Chạy script:**
```bash
mysql -u root -p shopgaubong < image_migration.sql
```

---

## 🚀 Cách sử dụng

### Cho Admin/Staff:

#### **Thêm sản phẩm mới:**
1. Click "Thêm mới"
2. Nhập thông tin sản phẩm (SKU, tên, giá, v.v.)
3. Click nút **"Chọn ảnh"**
4. Chọn file ảnh từ máy tính (JPG/PNG/GIF/BMP, max 5MB)
5. Preview ảnh sẽ hiển thị
6. Click "Lưu" → Ảnh được convert sang Base64 và lưu vào DB

#### **Sửa sản phẩm:**
1. Chọn sản phẩm trong bảng
2. Ảnh hiện tại sẽ hiển thị trong preview (nếu có)
3. Click "Chọn ảnh" để thay đổi ảnh mới
4. Click "Lưu"

#### **Xem ảnh:**
- Khi chọn sản phẩm, ảnh tự động hiển thị trong ImageView
- Nếu chưa có ảnh, hiển thị "Chưa có ảnh"

---

## 📋 Validation

### Kiểm tra khi upload ảnh:
✅ File phải là ảnh (JPG, JPEG, PNG, GIF, BMP)  
✅ Kích thước file ≤ 5MB  
✅ Tự động resize xuống 800x800px (giữ tỷ lệ)  
✅ Preview trước khi lưu  

### Thông báo lỗi:
- ❌ "File không phải là ảnh hợp lệ!"
- ❌ "Kích thước ảnh quá lớn! Vui lòng chọn ảnh nhỏ hơn 5MB."
- ❌ "Không thể tải ảnh: [lỗi chi tiết]"

---

## 💡 Ưu điểm

### So với lưu URL:
✅ **Không phụ thuộc server bên ngoài** - Ảnh lưu trực tiếp trong DB  
✅ **Không lo link ảnh bị hỏng** - Dữ liệu luôn có sẵn  
✅ **Backup dễ dàng** - Backup DB = backup cả ảnh  
✅ **Bảo mật tốt hơn** - Ảnh không public trên internet  
✅ **Tự động resize** - Tiết kiệm dung lượng database  

### So với lưu file:
✅ **Đơn giản hơn** - Không cần quản lý folder uploads  
✅ **Di chuyển dễ dàng** - Chỉ cần DB, không cần copy folder  
✅ **Không lo path issue** - Không có vấn đề đường dẫn tương đối/tuyệt đối  

---

## ⚠️ Lưu ý

### Dung lượng:
- Ảnh được resize max 800x800px
- Sau convert Base64, kích thước tăng ~33%
- Ảnh 200KB → Base64 ~266KB
- LONGTEXT hỗ trợ lên đến 4GB (quá đủ)

### Performance:
- ✅ Load ảnh nhanh với ImageView cache
- ✅ Resize trước khi lưu → giảm dung lượng DB
- ⚠️ Nếu có hàng nghìn sản phẩm, nên cân nhắc CDN

### Best practices:
- ✅ Nên chọn ảnh có kích thước vừa phải (< 2MB)
- ✅ Định dạng JPG tốt hơn PNG về dung lượng
- ✅ Ảnh càng nhỏ, load càng nhanh

---

## 🔧 Troubleshooting

### Lỗi: "Không thể tải ảnh"
**Nguyên nhân:** File bị lỗi hoặc không đúng định dạng  
**Giải pháp:** Thử file ảnh khác hoặc convert sang JPG

### Lỗi: "Kích thước ảnh quá lớn"
**Nguyên nhân:** File > 5MB  
**Giải pháp:** Resize ảnh xuống trước khi upload

### Ảnh không hiển thị
**Nguyên nhân:** Base64 string bị lỗi  
**Giải pháp:** Chọn ảnh lại và lưu

### Database migration lỗi
**Nguyên nhân:** Chưa chạy script `image_migration.sql`  
**Giải pháp:** 
```bash
mysql -u root -p shopgaubong < image_migration.sql
```

---

## 📊 Kích thước ước tính

| Kích thước ảnh gốc | Sau resize (800x800) | Base64 (DB) |
|-------------------|---------------------|-------------|
| 100 KB            | ~80 KB              | ~106 KB     |
| 500 KB            | ~200 KB             | ~266 KB     |
| 1 MB              | ~300 KB             | ~400 KB     |
| 2 MB              | ~400 KB             | ~532 KB     |
| 5 MB              | ~500 KB             | ~665 KB     |

**Kết luận:** Với resize 800x800, mỗi ảnh chỉ chiếm ~300-500KB trong DB. Với 1000 sản phẩm, tổng dung lượng ảnh chỉ ~300-500MB.

---

## ✅ Checklist hoàn thành

- [x] Tạo ImageUtil class
- [x] Thay đổi Item entity (imageUrl → imageData)
- [x] Cập nhật ItemService
- [x] Cập nhật ItemManagementController
- [x] Cập nhật FXML với Button + ImageView
- [x] Tạo migration SQL
- [x] Test validation
- [x] Tạo documentation

---

## 🎯 Kết quả

✅ **Upload ảnh từ máy hoạt động hoàn hảo!**  
✅ **Ảnh được lưu dưới dạng Base64 trong database**  
✅ **Preview ảnh trước khi lưu**  
✅ **Tự động resize để tiết kiệm dung lượng**  
✅ **Validation đầy đủ (file type, size)**  

**Tính năng sẵn sàng sử dụng! 🚀**

---

**Ngày hoàn thành:** 13/12/2025  
**Version:** 1.0
