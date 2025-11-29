# Hướng dẫn Cài đặt và Chạy UCOP - Shop Gấu Bông

## Yêu cầu hệ thống

1. **Java JDK 21 hoặc mới hơn**
   - Tải về từ: https://www.oracle.com/java/technologies/downloads/
   - Hoặc: https://adoptium.net/

2. **Apache Maven 3.6+**
   - Tải về từ: https://maven.apache.org/download.cgi
   
3. **MySQL 8.0+**
   - Tải về từ: https://dev.mysql.com/downloads/mysql/
   - Hoặc sử dụng XAMPP: https://www.apachefriends.org/

4. **IDE (tùy chọn)**
   - Eclipse IDE
   - IntelliJ IDEA
   - VS Code với Java Extension Pack

---

## Bước 1: Cài đặt Java

1. Tải và cài đặt Java JDK 21
2. Thiết lập biến môi trường JAVA_HOME:
   - Windows:
     ```
     JAVA_HOME = C:\Program Files\Java\jdk-21
     ```
   - Thêm vào PATH: `%JAVA_HOME%\bin`

3. Kiểm tra cài đặt:
   ```bash
   java -version
   ```

---

## Bước 2: Cài đặt Maven

### Cách 1: Cài đặt Maven
1. Tải Apache Maven từ trang chủ
2. Giải nén vào thư mục (ví dụ: C:\apache-maven)
3. Thêm vào PATH: `C:\apache-maven\bin`
4. Kiểm tra:
   ```bash
   mvn -version
   ```

### Cách 2: Sử dụng Maven Wrapper (đã có sẵn)
Nếu không muốn cài Maven, có thể dùng Maven wrapper có sẵn trong project:
```bash
.\mvnw.cmd clean install
```

---

## Bước 3: Cài đặt MySQL

### Nếu dùng XAMPP:
1. Tải và cài đặt XAMPP
2. Mở XAMPP Control Panel
3. Start Apache và MySQL
4. Mở phpMyAdmin: http://localhost/phpmyadmin

### Nếu dùng MySQL Standalone:
1. Tải và cài đặt MySQL Server
2. Nhớ mật khẩu root đã đặt khi cài đặt

---

## Bước 4: Tạo Database

### Cách 1: Dùng phpMyAdmin
1. Mở phpMyAdmin
2. Click "New" để tạo database mới
3. Tên database: `shopgaubong`
4. Collation: `utf8mb4_unicode_ci`
5. Click "Create"

### Cách 2: Dùng MySQL Command Line
```sql
CREATE DATABASE shopgaubong CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Cách 3: Chạy file SQL
```bash
mysql -u root -p < database_setup.sql
```

---

## Bước 5: Cấu hình Database Connection

Mở file `src/main/resources/META-INF/persistence.xml` và cập nhật:

```xml
<property name="jakarta.persistence.jdbc.url" 
          value="jdbc:mysql://localhost:3306/shopgaubong?useSSL=false&amp;serverTimezone=Asia/Ho_Chi_Minh&amp;characterEncoding=utf8mb4"/>
<property name="jakarta.persistence.jdbc.user" value="root"/>
<property name="jakarta.persistence.jdbc.password" value=""/>
```

**Lưu ý:** 
- Nếu MySQL có mật khẩu, thay `value=""` bằng mật khẩu của bạn
- Nếu port khác 3306, thay đổi trong URL

---

## Bước 6: Build Project

Mở terminal/command prompt tại thư mục project:

```bash
cd C:\Users\PC\eclipse-workspace\shopgaubong
```

### Nếu đã cài Maven:
```bash
mvn clean install
```

### Nếu dùng Maven Wrapper:
```bash
.\mvnw.cmd clean install
```

Lần đầu chạy sẽ tải về tất cả dependencies, có thể mất vài phút.

---

## Bước 7: Chạy ứng dụng

### Cách 1: Chạy bằng Maven
```bash
mvn javafx:run
```

hoặc

```bash
.\mvnw.cmd javafx:run
```

### Cách 2: Chạy từ IDE

#### Eclipse:
1. Right-click vào project → Run As → Java Application
2. Chọn class `Launcher` (com.example.shopgaubong.Launcher)

#### IntelliJ IDEA:
1. Mở class `Launcher.java`
2. Click nút Run (hoặc Shift+F10)

---

## Bước 8: Đăng nhập

Ứng dụng sẽ tự động tạo các tài khoản mặc định:

| Vai trò | Username | Password |
|---------|----------|----------|
| Admin | admin | admin123 |
| Staff | staff | staff123 |
| Customer | customer | customer123 |

---

## Xử lý lỗi thường gặp

### Lỗi 1: "Module not found"
**Nguyên nhân:** Dependencies chưa được tải
**Giải pháp:** Chạy `mvn clean install` hoặc refresh project trong IDE

### Lỗi 2: "Connection refused"
**Nguyên nhân:** MySQL chưa chạy hoặc cấu hình sai
**Giải pháp:** 
- Kiểm tra MySQL đã chạy chưa
- Kiểm tra username/password trong persistence.xml
- Kiểm tra port MySQL (mặc định: 3306)

### Lỗi 3: "Access denied for user"
**Nguyên nhân:** Sai mật khẩu MySQL
**Giải pháp:** Cập nhật mật khẩu trong persistence.xml

### Lỗi 4: "JAVA_HOME not found"
**Nguyên nhân:** Biến môi trường JAVA_HOME chưa được thiết lập
**Giải pháp:** 
1. Thiết lập JAVA_HOME trỏ đến thư mục JDK
2. Restart terminal/command prompt

### Lỗi 5: Database không tự động tạo bảng
**Nguyên nhân:** Hibernate chưa khởi tạo đúng
**Giải pháp:**
- Kiểm tra log khi chạy ứng dụng
- Kiểm tra file persistence.xml
- Đảm bảo database đã được tạo

---

## Kiểm tra Cài đặt

### 1. Kiểm tra Java:
```bash
java -version
# Output: java version "21.x.x"
```

### 2. Kiểm tra Maven:
```bash
mvn -version
# Output: Apache Maven 3.x.x
```

### 3. Kiểm tra MySQL:
```bash
mysql --version
# Output: mysql Ver 8.x.x
```

### 4. Kiểm tra kết nối MySQL:
```bash
mysql -u root -p
# Nhập mật khẩu, nếu kết nối thành công sẽ vào MySQL prompt
```

---

## Cấu trúc Project sau khi build

```
shopgaubong/
├── src/
├── target/              # Thư mục build output
├── logs/                # Thư mục log (tự động tạo khi chạy)
├── pom.xml
├── README.md
├── SETUP.md             # File này
└── database_setup.sql
```

---

## Chạy trong Eclipse

1. **Import Project:**
   - File → Import → Existing Maven Projects
   - Chọn thư mục shopgaubong
   - Click Finish

2. **Update Maven Project:**
   - Right-click vào project
   - Maven → Update Project
   - Check "Force Update of Snapshots/Releases"
   - OK

3. **Run Application:**
   - Right-click vào `Launcher.java`
   - Run As → Java Application

---

## Tài liệu tham khảo

- [JavaFX Documentation](https://openjfx.io/)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [Maven Documentation](https://maven.apache.org/guides/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

---

## Hỗ trợ

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra log trong thư mục `logs/`
2. Kiểm tra MySQL log
3. Đọc thông báo lỗi trong console

---

**Chúc bạn cài đặt thành công!**

