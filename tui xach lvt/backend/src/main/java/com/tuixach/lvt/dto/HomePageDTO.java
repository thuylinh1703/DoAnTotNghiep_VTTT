package com.tuixach.lvt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageDTO {
    private List<BannerDTO> banners;
    private List<CategoryDTO> categories;
    private List<ProductDTO> featuredProducts;
    private List<ProductDTO> newProducts;
    private List<ProductDTO> discountedProducts;
}
