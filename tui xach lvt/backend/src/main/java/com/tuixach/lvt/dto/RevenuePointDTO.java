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
public class RevenuePointDTO {
    private String period;
    private BigDecimal revenue;
    private long orderCount;
    private BigDecimal avgOrderValue;
}
