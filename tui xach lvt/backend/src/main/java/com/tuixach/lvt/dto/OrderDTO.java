package com.tuixach.lvt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderCode;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String paymentMethod;
    private String status;
    private BigDecimal totalAmount;
    private String couponCode;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String note;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
}
