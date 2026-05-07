package com.tuixach.lvt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull(message = "Mã sản phẩm không được để trống")
    private Long productId;

    @Min(value = 1, message = "Đánh giá tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa 5 sao")
    private int rating;

    @Size(min = 10, max = 1000, message = "Nội dung đánh giá từ 10 đến 1000 ký tự")
    private String comment;

    private java.util.List<String> images;
}
