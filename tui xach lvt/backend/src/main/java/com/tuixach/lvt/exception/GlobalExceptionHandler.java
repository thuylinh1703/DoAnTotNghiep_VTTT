package com.tuixach.lvt.exception;

import com.tuixach.lvt.dto.ApiResponse;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleInsufficientStock(InsufficientStockException ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("code", "INSUFFICIENT_STOCK");
        data.put("productId", ex.getProductId());
        data.put("productName", ex.getProductName());
        data.put("available", ex.getAvailable());
        ApiResponse<Map<String, Object>> body = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message(ex.getMessage())
                .data(data)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(CouponException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleCouponException(CouponException ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("code", ex.getCode().name());
        ApiResponse<Map<String, Object>> body = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message(ex.getMessage())
                .data(data)
                .build();
        HttpStatus status = ex.getCode() == CouponException.Code.COUPON_USAGE_LIMIT_REACHED
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ReviewException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleReviewException(ReviewException ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("code", ex.getCode().name());
        ApiResponse<Map<String, Object>> body = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message(ex.getMessage())
                .data(data)
                .build();

        HttpStatus status = switch (ex.getCode()) {
            case ALREADY_REVIEWED -> HttpStatus.CONFLICT;
            case NOT_PURCHASED, NOT_DELIVERED -> HttpStatus.FORBIDDEN;
        };
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(OptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Dữ liệu đã được cập nhật bởi thao tác khác, vui lòng thử lại"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        String firstErrorMessage = "Dữ liệu không hợp lệ";
        
        if (ex.getBindingResult().hasErrors()) {
            firstErrorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message(firstErrorMessage)
                .data(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống: " + ex.getMessage()));
    }
}
