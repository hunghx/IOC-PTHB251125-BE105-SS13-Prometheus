# QuickBite Microservices Monitoring Demo (Session 13)

Dự án mẫu minh họa đầy đủ cấu hình **Prometheus Monitoring** cho ứng dụng Microservice Spring Boot và Hạ tầng VPS theo giáo trình Session 13.

---

## 🛠 1. Công nghệ & Cấu trúc Dự án

- **JDK**: Java 17
- **Build Tool**: Gradle 8.x
- **Framework**: Spring Boot 3.2.3 (Web, Actuator, Micrometer Prometheus)
- **Containerization**: Docker Compose
- **Monitoring**: Prometheus v2.45.0, Node Exporter v1.6.0

```
quickbite-monitoring-demo/
├── build.gradle
├── settings.gradle
├── Dockerfile
├── docker-compose.yml
├── prometheus/
│   └── prometheus.yml
└── src/
    └── main/
        ├── java/com/quickbite/order/
        │   ├── OrderServiceApplication.java
        │   └── controller/OrderController.java
        └── resources/
            └── application.yml
```

---

## 🚀 2. Các Bước Khởi chạy & Kiểm tra

### Bước 1: Khởi chạy bằng Docker Compose
Mở Terminal tại thư mục `quickbite-monitoring-demo` và thực thi:
```bash
docker compose up -d --build
```

### Bước 2: Kiểm tra trạng thái Container
```bash
docker compose ps
```
Đảm bảo 3 dịch vụ `quickbite-order-service`, `quickbite-prometheus`, và `quickbite-node-exporter` đều ở trạng thái `Up`.

### Bước 3: Tạo thử nghiệm dữ liệu Metrics (Tương tác API)
- Tạo 5 đơn hàng mới:
  ```bash
  curl -X POST http://localhost:8083/api/v1/orders?item=Burger
  ```
- Hoàn tất 1 đơn hàng:
  ```bash
  curl -X POST http://localhost:8083/api/v1/orders/complete
  ```

### Bước 4: Kiểm tra Actuator Prometheus Endpoint
Truy cập qua Trình duyệt hoặc Curl:
```bash
curl http://localhost:8083/actuator/prometheus
```
Bạn sẽ thấy các chỉ số xuất ra dạng text format Prometheus, ví dụ:
```text
# HELP quickbite_orders_created_total Tổng số đơn hàng được tạo thành công trong hệ thống QuickBite
# TYPE quickbite_orders_created_total counter
quickbite_orders_created_total{application="order-service",service="order-service",} 5.0

# HELP quickbite_active_orders 
# TYPE quickbite_active_orders gauge
quickbite_active_orders{application="order-service",} 4.0
```

### Bước 5: Truy cập Prometheus Dashboard & API Targets
- Truy cập giao diện Prometheus Web UI: `http://localhost:9090`
- Xem danh sách Targets: Truy cập Menu `Status -> Targets` hoặc chạy lệnh API:
  ```bash
  docker exec -it quickbite-prometheus wget -qO- http://localhost:9090/api/v1/targets
  ```

---

## 🔒 3. Lưu ý Bảo mật Production (Session 13)
1. **Actuator Expose**: Chỉ include `health` và `prometheus` trong `application.yml`.
2. **Không mở cổng công cộng**: Prometheus & Node Exporter giao tiếp thông qua Docker Network nội bộ `quickbite-net`. Cổng `9090` và `9100` không nên publish ra Internet bên ngoài Host.
3. **Mount Read-Only (`:ro`)**: Node Exporter chỉ đọc các thư mục `/proc`, `/sys`, `/` của Host để ngăn chặn nguy cơ can thiệp hệ điều hành.
