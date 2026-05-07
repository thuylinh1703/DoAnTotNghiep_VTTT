package com.tuixach.lvt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CouponValidateRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    private String code;
}
