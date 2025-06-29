# 🏨 Hệ thống quản lý và đặt phòng khách sạn tích hợp AI tư vấn khách hàng trực tuyến

Phát triển bằng **Java Spring Boot** (backend) và **ReactJS** (frontend).
---

## 🚀 Link chạy thử

- 🔗 Giao diện người dùng (FE): [https://hotelstar.vercel.app/client/home](https://hotelstar.vercel.app/client/home)
---
### ⚠️ Lưu ý khi truy cập demo
Trang web đôi khi load chậm hoặc không truy cập được do Render tự động sleep sau một thời gian không hoạt động (free tier).
Dù đã cài đặt UptimeRobot để gọi API định kỳ, nhưng tình trạng "sleep" vẫn có thể xảy ra.
Vui lòng thử lại sau vài phút nếu gặp lỗi.

## ⚙️ Công nghệ sử dụng

- **Backend**: Java 21, Spring Boot, Spring Security, JWT, Maven
- **Frontend**: ReactJS, Bootstrap 5
- **Database**: MySQL (hosted trên TiDB Cloud)
- **Thanh toán**: Tích hợp VNPAY API
- **Trợ lý AI**: Gemini (Google AI API)
- **Triển khai**: Docker + Render (backend), Vercel (frontend)

---

## 🌟 Tính năng nổi bật

- Đăng ký, đăng nhập (bao gồm Google OAuth)
- Tìm kiếm, lọc và đặt phòng theo ngày, số khách
- Chatbot AI hỗ trợ khách hàng 24/7 (Gemini)
- Thanh toán trực tuyến qua VNPAY
- Xem lịch sử đặt phòng, hủy phòng, cập nhật thông tin cá nhân

---

## 🧪 Cách chạy backend tại máy cá nhân

### 🔧 Yêu cầu

- Java 21 trở lên  
- Maven  
- Kết nối Internet (vì sử dụng database online)

---

### 📁 Thông tin kết nối CSDL (cấu hình sẵn trong `application.properties`)


