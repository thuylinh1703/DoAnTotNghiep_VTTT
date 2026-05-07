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
public class RevenueSummaryDTO {
    private BigDecimal today;
    private BigDecimal yesterday;
    private BigDecimal thisWeek;
    private BigDecimal lastWeek;
    private BigDecimal thisMonth;
    private BigDecimal lastMonth;
    private BigDecimal yoy;
}
