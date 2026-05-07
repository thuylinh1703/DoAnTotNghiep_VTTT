package com.tuixach.lvt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private Long orderItemId;
    private boolean verifiedPurchase;
    private int rating;
    private String comment;
    private java.util.List<String> images;
    private boolean active;
    private LocalDateTime createdAt;
}
