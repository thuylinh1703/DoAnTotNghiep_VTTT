package com.tuixach.lvt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_revenue_summaries", indexes = {
    @Index(name = "idx_daily_rev_date", columnList = "summary_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRevenueSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "summary_date", nullable = false, unique = true)
    private LocalDate date;

    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;

    @Builder.Default
    private int orderCount = 0;

    @Builder.Default
    private int productCount = 0;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal netRevenue = BigDecimal.ZERO;
}
