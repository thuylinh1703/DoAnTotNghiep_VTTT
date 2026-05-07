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
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String brand;
    private int quantity;
    private boolean featured;
    private boolean active;
    private Long categoryId;
    private String categoryName;
    private List<String> imageUrls;
    private String primaryImage;
    private BigDecimal averageRating;
    private int reviewCount;
    private LocalDateTime createdAt;
}
