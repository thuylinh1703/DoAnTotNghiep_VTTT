package com.tuixach.lvt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank(message = "Họ tên người nhận không được để trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String receiverAddress;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // COD or BANK_TRANSFER

    private String note;

    private String couponCode;
}
