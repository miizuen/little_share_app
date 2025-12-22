# Hướng dẫn tạo Firebase Index

## Vấn đề hiện tại
Lỗi: `FAILED_PRECONDITION: The query requires an index`

## Giải pháp: Tạo Composite Index

### Cách 1: Qua Firebase Console
1. Truy cập [Firebase Console](https://console.firebase.google.com)
2. Chọn project: `littleshare-7b64c`
3. Vào **Firestore Database** → **Indexes**
4. Click **Create Index**
5. Điền thông tin:
   - **Collection ID**: `sponsorDonations`
   - **Fields**:
     - Field 1: `sponsorId` - **Ascending**
     - Field 2: `donationDate` - **Descending**
6. Click **Create**

### Cách 2: Qua Error Link
1. Khi chạy app và gặp lỗi, trong log sẽ có link tự động tạo index
2. Click vào link đó để tự động tạo index

### Thời gian chờ
- Index sẽ mất **2-5 phút** để build xong
- Trạng thái sẽ chuyển từ "Building" → "Enabled"

### Kiểm tra sau khi tạo xong
1. Thực hiện donation mới
2. Kiểm tra campaigns hiện trong "Chiến dịch đang tài trợ"
3. Kiểm tra campaigns hiện trong "Chặng đường chia sẻ"
4. Kiểm tra số liệu thống kê trong header

## Lưu ý quan trọng
- **PHẢI** tạo index trước khi test các tính năng khác
- Index là nguyên nhân chính khiến campaigns không hiển thị
- Sau khi tạo xong, app sẽ hoạt động bình thường