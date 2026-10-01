# 📚 Online Book Reselling

## 📌 Giới thiệu dự án

**Online Book Reselling** là một website mua bán sách trực tuyến, cho phép người dùng tìm kiếm, xem, mua và bán sách thông qua Internet.

Người dùng có thể đăng ký tài khoản, đăng nhập, xem danh sách sách, thêm sách vào giỏ hàng, đặt mua sách và đăng sách của mình để bán.

Quản trị viên có thể quản lý người dùng, sách và các giao dịch trong hệ thống.

---

## 🎯 Mục tiêu dự án

* Xây dựng website mua bán sách trực tuyến.
* Giúp người dùng dễ dàng tìm kiếm và mua sách.
* Cho phép người dùng đăng bán những cuốn sách của mình.
* Quản lý sách, người dùng, giỏ hàng và đơn hàng.
* Cung cấp hệ thống quản lý dành cho Admin.

---

## 🛠️ Công nghệ sử dụng

### Backend

* Java 17
* Spring Boot
* Spring MVC
* Spring Data JPA
* Hibernate
* Spring Security

### Frontend

* HTML5
* CSS3
* Bootstrap 5
* Thymeleaf
* JavaScript

### Database

* MySQL 8

### Công cụ phát triển

* IntelliJ IDEA
* Gradle
* Git
* GitHub
* Postman

---

## 👤 Chức năng User

### 🔐 Đăng ký và đăng nhập

* Đăng ký tài khoản.
* Đăng nhập.
* Đăng xuất.

### 📚 Quản lý và xem sách

* Xem danh sách sách.
* Tìm kiếm sách.
* Xem chi tiết sách.
* Thêm sách để bán.
* Upload hình ảnh sách.

### 🛒 Mua sách

* Thêm sách vào giỏ hàng.
* Cập nhật số lượng.
* Xóa sách khỏi giỏ hàng.
* Checkout.
* Đặt mua sách.

### 📦 Quản lý đơn hàng

* Xem lịch sử mua hàng.
* Xem chi tiết đơn hàng.

### 💰 Bán sách

* Thêm sách muốn bán.
* Upload thông tin và hình ảnh sách.
* Thiết lập giá bán.
* Quản lý sách đã đăng bán.
* Xem thông tin người mua liên quan đến sách của mình.

---

## 👨‍💼 Chức năng Admin

### 🔐 Admin Login

* Đăng nhập vào hệ thống quản trị.

### 📚 Quản lý sách

* Xem danh sách sách.
* Xem thông tin chi tiết sách.

### 👥 Quản lý người dùng

* Xem danh sách người dùng.
* Xem thông tin người dùng.

### 💳 Quản lý giao dịch

* Xem thông tin các giao dịch mua bán.
* Xem thông tin đơn hàng.

---

## 📋 WBS của dự án

```text
[1.0] Online Book Reselling
│
├── [1.1] Project Management
│
├── [1.2] Requirements Analysis
│
├── [1.3] System Design
│
├── [1.4] User Module
│   ├── Registration
│   ├── Login
│   ├── View Books
│   ├── Buy Books
│   ├── Add Books
│   ├── Upload Books
│   ├── My Orders
│   └── View Buyer
│
├── [1.5] Admin Module
│   ├── Admin Login
│   ├── View Books
│   ├── View Transactions
│   └── View Users
│
├── [1.6] Testing
│   ├── Unit Testing
│   ├── Integration Testing
│   └── System Testing
│
└── [1.7] Deployment & Documentation
    ├── Deployment
    └── Documentation
```

---

## ⚙️ Cài đặt và chạy dự án

### 1. Clone repository

```bash
git clone <GITHUB_REPOSITORY_URL>
```

### 2. Mở project

Mở project bằng **IntelliJ IDEA**.

### 3. Tạo Database

Mở MySQL và tạo database:

```sql
CREATE DATABASE book_online;
```

### 4. Cấu hình Database

Mở file:

```text
src/main/resources/application.properties
```

Cấu hình:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/book_online
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.thymeleaf.cache=false
```

Thay `YOUR_PASSWORD` bằng mật khẩu MySQL của bạn.

### 5. Chạy project

Chạy class:

```text
BookOnlineApplication.java
```

Sau đó truy cập:

```text
http://localhost:8080
```

---

## 🌱 Git Branch

Dự án sử dụng Git và GitHub để quản lý source code.

Các branch dự kiến:

```text
main
│
├── develop
│
├── feature/authentication
├── feature/book-management
├── feature/cart
├── feature/order
└── feature/admin
```

Ví dụ tạo branch cho chức năng đăng nhập:

```bash
git checkout -b feature/authentication
```

Commit:

```bash
git add .
git commit -m "feat: implement user authentication"
```

Push:

```bash
git push origin feature/authentication
```

---

## 👨‍💻 Nhóm phát triển

**Nguyễn Vinh Thiện**
**Nguyễn Ngọc Hiền**
**Cao Lê Ngọc Triều**
**Nguyễn Văn Hoàng Hướng**

Sinh viên lớp CMU-TPM5 ngành khoa học máy tính

**Duy Tân University**
