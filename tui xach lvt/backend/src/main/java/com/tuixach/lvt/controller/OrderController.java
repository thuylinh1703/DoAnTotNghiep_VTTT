package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.*;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderRequest request) {
        OrderDTO order = orderService.createOrder(user, request);
        return ResponseEntity.ok(ApiResponse.success("Đặt hàng thành công", order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getUserOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OrderDTO> orders = orderService.getUserOrders(user, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByCode(@PathVariable String orderCode) {
        OrderDTO order = orderService.getOrderByCode(orderCode);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PutMapping("/{orderCode}/confirm-received")
    public ResponseEntity<ApiResponse<OrderDTO>> confirmReceived(
            @AuthenticationPrincipal User user,
            @PathVariable String orderCode) {
        OrderDTO order = orderService.confirmReceived(user, orderCode);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận đã nhận hàng thành công", order));
    }
}
