package com.quickbite.order.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final Counter orderCounter;
    private final AtomicInteger activeOrdersGauge;

    public OrderController(MeterRegistry registry) {
        // Ví dụ 1: Counter - Chỉ tăng để đếm tổng số đơn hàng đã tạo
        this.orderCounter = Counter.builder("quickbite_orders_created_total")
                .description("Tổng số đơn hàng được tạo thành công trong hệ thống QuickBite")
                .tag("service", "order-service")
                .register(registry);

        // Ví dụ 2: Gauge - Tăng/Giảm biến động đo số đơn hàng đang xử lý tại thời điểm hiện tại
        this.activeOrdersGauge = registry.gauge(
                "quickbite_active_orders",
                new AtomicInteger(0)
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestParam(defaultValue = "Burger Combo") String item) {
        // Tăng Counter mỗi khi tạo đơn mới
        orderCounter.increment();
        
        // Tăng Gauge số đơn đang xử lý
        int currentActive = activeOrdersGauge.incrementAndGet();

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Đặt hàng thành công",
                "item", item,
                "currentActiveOrders", currentActive
        ));
    }

    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completeOrder() {
        int currentActive = activeOrdersGauge.decrementAndGet();
        if (currentActive < 0) {
            activeOrdersGauge.set(0);
            currentActive = 0;
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Đã hoàn tất 1 đơn hàng",
                "currentActiveOrders", currentActive
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        return ResponseEntity.ok(Map.of(
                "service", "order-service",
                "status", "RUNNING",
                "activeOrders", activeOrdersGauge.get()
        ));
    }
}
