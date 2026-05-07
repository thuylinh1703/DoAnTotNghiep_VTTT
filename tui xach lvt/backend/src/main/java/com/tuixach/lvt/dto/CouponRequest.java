package com.tuixach.lvt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CouponRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(max = 40, message = "Mã tối đa 40 ký tự")
    private String code;

    @Size(max = 500)
    private String description;

    @NotBlank(message = "Loại giảm giá không được để trống")
    private String type;

    @NotNull(message = "Giá trị giảm không được để trống")
    @Positive(message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal value;

    @PositiveOrZero
    private BigDecimal minOrderAmount;

    @PositiveOrZero
    private BigDecimal maxDiscountAmount;

    @PositiveOrZero
    private Integer usageLimit;

    @PositiveOrZero
    private Integer perUserLimit;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    private Boolean active;

    private List<Long> categoryIds;

    private List<Long> productIds;
}
