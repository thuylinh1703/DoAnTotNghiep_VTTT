package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.*;
import com.tuixach.lvt.service.BannerService;
import com.tuixach.lvt.service.CategoryService;
import com.tuixach.lvt.service.CouponService;
import com.tuixach.lvt.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final BannerService bannerService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<ApiResponse<HomePageDTO>> getHomePage() {
        List<BannerDTO> banners = bannerService.getActiveBanners();
        List<CategoryDTO> categories = categoryService.getActiveCategories();
        List<ProductDTO> featuredProducts = productService.getFeaturedProducts();
        List<ProductDTO> newProducts = productService.getNewProducts(8);
        List<ProductDTO> discountedProducts = productService.getDiscountedProducts();

        HomePageDTO homePage = HomePageDTO.builder()
                .banners(banners)
                .categories(categories)
                .featuredProducts(featuredProducts)
                .newProducts(newProducts)
                .discountedProducts(discountedProducts)
                .build();

        return ResponseEntity.ok(ApiResponse.success(homePage));
    }

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getAvailableCoupons() {
        return ResponseEntity.ok(ApiResponse.success(couponService.getAvailableCoupons()));
    }
}
