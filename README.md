# 📱 Phone Store Backend

Backend cho hệ thống bán điện thoại trực tuyến được phát triển bằng Spring Boot 3, cung cấp REST API cho website bán hàng cùng các chức năng quản trị, thanh toán, chat realtime và chatbot hỗ trợ khách hàng.

---

## 🚀 Công nghệ sử dụng

### Backend
- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Spring Validation
- Spring WebSocket (STOMP + SockJS)
- JWT Authentication
- MapStruct
- Lombok

### Database
- MySQL

### Third-party Services
- Google OAuth Login
- Brevo Email API

### Export Data
- Apache POI (Excel)

---

## 📌 Chức năng chính

### 👤 Authentication & Authorization

- Đăng ký tài khoản
- Xác thực email
- Đăng nhập bằng JWT
- Đăng nhập bằng Google
- Quên mật khẩu bằng OTP
- Phân quyền người dùng

---

### 📱 Quản lý sản phẩm

- Danh sách sản phẩm
- Chi tiết sản phẩm
- Danh mục sản phẩm
- Thương hiệu sản phẩm
- Quản lý tồn kho
- Tìm kiếm sản phẩm

---

### 🛒 Giỏ hàng

- Thêm sản phẩm vào giỏ hàng
- Cập nhật số lượng
- Xóa sản phẩm khỏi giỏ hàng
- Xem giỏ hàng hiện tại

---

### 📦 Đơn hàng

- Tạo đơn hàng
- Quản lý trạng thái đơn hàng
- Xem lịch sử mua hàng
- Chi tiết đơn hàng

---

### 💳 Thanh toán

- Tạo thanh toán
- Theo dõi trạng thái thanh toán
- Quản lý phương thức thanh toán
- Scheduler tự động xử lý thanh toán

---

### 🏢 Quản lý nhập hàng

- Quản lý nhà cung cấp
- Tạo phiếu nhập
- Chi tiết nhập hàng
- Cập nhật tồn kho sau nhập

---

### ⭐ Đánh giá sản phẩm

- Gửi đánh giá sản phẩm
- Quản lý phản hồi khách hàng

---

### 🎖️ Membership System

- Quản lý hạng thành viên
- Lưu lịch sử thay đổi hạng
- Chính sách khách hàng thân thiết

---

### 🔔 Notification

- Gửi thông báo cho người dùng
- Quản lý trạng thái thông báo

---

### 💬 Chat Realtime

- Chat trực tiếp giữa khách hàng và nhân viên
- Lưu lịch sử tin nhắn
- Quản lý phòng chat
- Đồng bộ dữ liệu theo thời gian thực bằng WebSocket

#### WebSocket Endpoint

```text
/ws
```

#### Broker

```text
/topic/**
```

#### Application Prefix

```text
/app/**
```

---

### 🤖 Chatbot

- Hỗ trợ tư vấn khách hàng
- Trả lời câu hỏi cơ bản về sản phẩm và dịch vụ

---

### 📊 Dashboard

- Thống kê doanh thu
- Thống kê đơn hàng
- Thống kê người dùng
- Báo cáo tổng quan hệ thống

---

### 📄 Export Excel

Hệ thống hỗ trợ xuất dữ liệu Excel cho:

- Users
- Products
- Orders
- Payments
- Import Receipts

---

## 🗂️ Cấu trúc dự án

```text
src/main/java/com/ngtoan/phone_store

├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── mapper
├── repository
├── scheduler
├── security
├── service
├── util
└── exception
```

---

## 🏛️ Kiến trúc

```text
Client
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
MySQL Database
```

Dự án áp dụng mô hình phân tầng (Layered Architecture) giúp dễ bảo trì và mở rộng.

---

## 🗄️ Các thực thể chính

- User
- Role
- Product
- ProductDetail
- Category
- Brand
- Cart
- CartItem
- Order
- OrderDetail
- Payment
- PaymentMethod
- PaymentStatus
- Supplier
- ImportReceipt
- ImportDetail
- Feedback
- Notification
- MembershipLevel
- MembershipHistory
- ChatRoom
- ChatMessage

---

## 🔐 Bảo mật

- Spring Security
- JWT Authentication
- Role-based Authorization
- Email Verification
- Password Reset OTP
- Google OAuth Login

---

## ⚙️ Cài đặt

### 1. Clone project

```bash
git clone https://github.com/your-username/phone-store.git
```

### 2. Tạo database

```sql
CREATE DATABASE phone_store;
```

### 3. Cấu hình biến môi trường

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=

GOOGLE_CLIENT_ID=

BREVO_API_KEY=
BREVO_SENDER_EMAIL=
BREVO_SENDER_NAME=
```

### 4. Chạy project

```bash
mvn clean install
mvn spring-boot:run
```

Server mặc định:

```text
http://localhost:8080
```

---

## 📡 API Modules

| Module | Endpoint |
|----------|----------|
| Auth | `/api/auth/**` |
| Users | `/api/users/**` |
| Products | `/api/products/**` |
| Cart | `/api/cart/**` |
| Orders | `/api/orders/**` |
| Payments | `/api/payments/**` |
| Suppliers | `/api/suppliers/**` |
| Import Receipts | `/api/import-receipts/**` |
| Feedback | `/api/feedback/**` |
| Notifications | `/api/notifications/**` |
| Dashboard | `/api/dashboard/**` |
| Chat | `/api/chat/**` |
| Chatbot | `/api/chatbot/**` |

---

## 👨‍💻 Development Team

| Thành viên | Vai trò |
|------------|----------|
| Đặng Trần Ngọc Toàn | Backend Developer |
| Nguyễn Hữu Bảo Khiêm | Backend Developer |
| Trần Tuấn Anh | Frontend Developer |
| Lữ Quang Minh | Full Stack Developer |

## 📜 License

This project is developed for learning and academic purposes.
