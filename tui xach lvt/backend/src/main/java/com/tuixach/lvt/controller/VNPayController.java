package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.ApiResponse;
import com.tuixach.lvt.service.OrderService;
import com.tuixach.lvt.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayController {

    private final VNPayService vnPayService;
    private final OrderService orderService;

    @GetMapping("/create-payment/{orderCode}")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPayment(
            HttpServletRequest request,
            @PathVariable String orderCode,
            @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        
        var order = orderService.getOrderByCode(orderCode);
        // Charge the discounted amount when a coupon is applied — otherwise
        // VNPay would collect pre-discount value and we'd owe the customer back.
        long amount = (order.getFinalAmount() != null
                ? order.getFinalAmount()
                : order.getTotalAmount()).longValue();
        
        String paymentUrl = vnPayService.createPaymentUrl(request, amount, orderCode, returnUrl);
        
        Map<String, String> result = new HashMap<>();
        result.put("paymentUrl", paymentUrl);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/vnpay-payment-return")
    public ResponseEntity<ApiResponse<Map<String, Object>>> paymentReturn(HttpServletRequest request) {
        int paymentStatus = vnPayService.orderReturn(request);
        
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");
        String responseCode = request.getParameter("vnp_ResponseCode");

        Map<String, Object> result = new HashMap<>();
        result.put("orderInfo", orderInfo);
        result.put("totalPrice", totalPrice);
        result.put("paymentTime", paymentTime);
        result.put("transactionId", transactionId);
        result.put("status", paymentStatus == 1 ? "success" : "fail");
        result.put("responseCode", responseCode);

        String orderCode = request.getParameter("vnp_TxnRef");
        if (paymentStatus == 1 && orderCode != null) {
            try {
                orderService.updateOrderStatusByCode(orderCode, "CONFIRMED");
                log.info("Successfully processed VNPay payment for order {}", orderCode);
            } catch (Exception e) {
                log.error("Failed to update order status after VNPay success: {}", e.getMessage());
                result.put("status", "error");
                result.put("message", "Payment success but failed to update order status. Please contact support.");
            }
        } else if (paymentStatus != 1 && orderCode != null) {
            // Payment failed or cancelled — restore stock (idempotent).
            try {
                boolean cancelled = orderService.cancelIfPending(orderCode,
                        "VNPay thất bại (mã " + responseCode + ")");
                if (cancelled) {
                    log.info("Cancelled order {} after VNPay failure, stock restored", orderCode);
                }
            } catch (Exception e) {
                log.error("Failed to cancel order {} after VNPay failure: {}", orderCode, e.getMessage());
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
