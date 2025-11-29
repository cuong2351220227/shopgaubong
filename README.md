# UCOP - Shop Gấu Bông

Nền tảng quản lý tổng quát cho các hoạt động kinh doanh bán lẻ (Sản phẩm → Đơn hàng → Giao hàng).

## Công nghệ sử dụng

- **Java**: Ngôn ngữ lập trình chính
- **JavaFX**: Giao diện người dùng
- **Hibernate/JPA**: ORM framework
- **MySQL**: Cơ sở dữ liệu
- **Maven**: Quản lý dependencies
- **BCrypt**: Mã hóa mật khẩu
- **OpenCSV**: Import/Export CSV
- **Apache POI**: Import/Export Excel

## Kiến trúc

Mô hình 3-Layer:
- **Entity Layer**: Các entity classes với Hibernate annotations
- **DAO Layer**: Data Access Objects - truy xuất dữ liệu
- **Service Layer**: Business logic
- **Controller Layer**: JavaFX controllers - xử lý UI

## Cấu trúc Database

### Các bảng chính:
- `accounts`: Tài khoản đăng nhập
- `account_profiles`: Thông tin cá nhân
- `categories`: Danh mục sản phẩm (hỗ trợ đa cấp)
- `items`: Sản phẩm
- `warehouses`: Kho
- `stock_items`: Tồn kho
- `orders`: Đơn hàng
- `order_items`: Chi tiết đơn hàng
- `payments`: Thanh toán
- `shipments`: Vận chuyển
- `promotions`: Khuyến mãi

## Cài đặt và Chạy

### Yêu cầu:
- Java JDK 21+
- Maven 3.6+
- MySQL 8.0+
- Scene Builder (tùy chọn - để chỉnh sửa FXML)

### Bước 1: Cấu hình Database

Tạo database MySQL:
```sql
CREATE DATABASE shopgaubong CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Cập nhật thông tin kết nối trong `src/main/resources/META-INF/persistence.xml`:
```xml
<property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/shopgaubong?useSSL=false&amp;serverTimezone=Asia/Ho_Chi_Minh&amp;characterEncoding=utf8mb4"/>
<property name="jakarta.persistence.jdbc.user" value="root"/>
<property name="jakarta.persistence.jdbc.password" value=""/>
```

### Bước 2: Build Project

```bash
mvn clean install
```

### Bước 3: Chạy ứng dụng

```bash
mvn javafx:run
```

Hoặc chạy class `Launcher.java` từ IDE.

## Tài khoản mặc định

Ứng dụng sẽ tự động tạo các tài khoản mặc định khi chạy lần đầu:

| Vai trò | Username | Password |
|---------|----------|----------|
| Admin | admin | admin123 |
| Staff | staff | staff123 |
| Customer | customer | customer123 |

## Phân quyền

### Admin (Quản trị viên)
- Quản lý người dùng
- Quản lý danh mục và sản phẩm
- Quản lý kho
- Quản lý đơn hàng
- Quản lý khuyến mãi
- Xem báo cáo

### Staff (Nhân viên)
- Xử lý đơn hàng
- Quản lý kho
- Xử lý vận chuyển

### Customer (Khách hàng)
- Xem sản phẩm
- Tạo giỏ hàng
- Đặt hàng
- Theo dõi đơn hàng
- Quản lý thông tin cá nhân

## Tính năng chính

### Quản lý Người dùng & Phân quyền
- ✅ Đăng nhập/Đăng xuất
- ✅ Phân quyền theo vai trò (RBAC)
- ✅ Mã hóa mật khẩu với BCrypt
- Đổi mật khẩu
- Khóa/Mở khóa tài khoản

### Quản lý Danh mục & Sản phẩm
- CRUD Danh mục (hỗ trợ đa cấp)
- CRUD Sản phẩm
- Import/Export CSV
- Tìm kiếm sản phẩm

### Quản lý Kho & Tồn
- CRUD Kho
- CRUD Tồn kho
- Cảnh báo tồn kho thấp
- Quản lý số lượng giữ chỗ (Reserved)

### Giỏ hàng & Đặt hàng
- Quản lý giỏ hàng
- Áp dụng mã giảm giá
- Tính toán tự động (Thuế, Phí ship, Chiết khấu)
- Quản lý địa chỉ giao hàng

### Quản lý Đơn hàng
- Theo dõi trạng thái đơn hàng
- Lọc và tìm kiếm đơn hàng
- Xử lý hủy/hoàn đơn

### Thanh toán
- Hỗ trợ nhiều phương thức: COD, Chuyển khoản, Gateway, Wallet
- Tính toán chi tiết các khoản phí
- Quản lý hoàn tiền

### Vận chuyển
- Quản lý mã vận đơn
- Theo dõi trạng thái vận chuyển
- Cập nhật ngày giao hàng

### Khuyến mãi
- Tạo mã giảm giá
- Giảm theo % hoặc số tiền cố định
- Giới hạn thời gian và số lượt sử dụng

### Báo cáo & Dashboard
- Báo cáo doanh thu
- Top sản phẩm bán chạy
- Báo cáo tồn kho
- Biểu đồ (sử dụng JavaFX Chart)

## Audit & Lịch sử
- Tự động lưu CreatedAt, UpdatedAt, CreatedBy, UpdatedBy
- Theo dõi lịch sử thay đổi

## Cấu trúc thư mục

```
shopgaubong/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/shopgaubong/
│   │   │       ├── controller/      # JavaFX Controllers
│   │   │       ├── dao/              # Data Access Objects
│   │   │       ├── entity/           # Entity Classes
│   │   │       │   └── base/         # Base Entity
│   │   │       ├── enums/            # Enums
│   │   │       ├── service/          # Business Logic
│   │   │       ├── util/             # Utility Classes
│   │   │       ├── HelloApplication.java
│   │   │       └── Launcher.java
│   │   └── resources/
│   │       ├── com/example/shopgaubong/  # FXML Files
│   │       ├── META-INF/
│   │       │   └── persistence.xml   # Hibernate config
│   │       └── logback.xml           # Logging config
│   └── test/
├── pom.xml
└── README.md
```

## Lưu ý

- Ứng dụng không sử dụng CSS, chỉ dùng inline styles trong FXML
- Database sẽ tự động tạo bảng khi chạy lần đầu (hibernate.hbm2ddl.auto = update)
- Tất cả text trong ứng dụng đều sử dụng Tiếng Việt
- Transaction được sử dụng cho các thao tác quan trọng (tạo đơn hàng, thanh toán)

## Phát triển tiếp

Các chức năng đang được phát triển:
- [ ] CRUD đầy đủ cho tất cả các module
- [ ] Import/Export CSV/Excel cho sản phẩm
- [ ] Dashboard với biểu đồ
- [ ] Tìm kiếm và lọc nâng cao
- [ ] In hóa đơn, phiếu xuất kho
- [ ] Email notification
- [ ] Backup/Restore database

## Liên hệ & Hỗ trợ

Để được hỗ trợ hoặc báo cáo lỗi, vui lòng tạo issue trên repository.

---
**UCOP - Shop Gấu Bông v1.0**

