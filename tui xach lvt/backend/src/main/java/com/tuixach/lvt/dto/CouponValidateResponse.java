package com.tuixach.lvt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidateResponse {
    private String code;
    private String type;
    private BigDecimal value;
    private BigDecimal eligibleSubtotal;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
    private String description;
}
