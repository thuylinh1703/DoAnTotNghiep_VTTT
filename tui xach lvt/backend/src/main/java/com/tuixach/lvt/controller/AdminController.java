package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.ApiResponse;
import com.tuixach.lvt.dto.DashboardDTO;
import com.tuixach.lvt.dto.ProductDTO;
import com.tuixach.lvt.dto.ProductRequest;
import com.tuixach.lvt.dto.CategoryDTO;
import com.tuixach.lvt.dto.CategoryRequest;
import com.tuixach.lvt.dto.OrderDTO;
import com.tuixach.lvt.dto.OrderRequest;
import com.tuixach.lvt.dto.UserDTO;
import com.tuixach.lvt.dto.ReviewDTO;
import com.tuixach.lvt.dto.BannerDTO;
import com.tuixach.lvt.dto.CouponDTO;
import com.tuixach.lvt.dto.CouponRequest;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.exception.BadRequestException;
import com.tuixach.lvt.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final BannerService bannerService;
    private final CouponService couponService;

    // ==================== Dashboard ====================
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboard(
            @RequestParam(required = false) Integer year) {
        int effectiveYear = (year != null) ? year : LocalDate.now().getYear();
        DashboardDTO dashboard = dashboardService.getDashboardData(effectiveYear);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    // ==================== Product Management ====================
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProductDTO> products = productService.getAllProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @RequestPart("product") ProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        ProductDTO product = productService.createProduct(request, images);
        return ResponseEntity.ok(ApiResponse.success("Thêm sản phẩm thành công", product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") ProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        ProductDTO product = productService.updateProduct(id, request, images);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", product));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null));
    }

    // ==================== Category Management ====================
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@RequestBody CategoryRequest request) {
        CategoryDTO category = categoryService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success("Thêm danh mục thành công", category));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id, @RequestBody CategoryRequest request) {
        CategoryDTO category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công", category));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công", null));
    }

    // ==================== Order Management ====================
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders(page, size, status, search)));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderDetail(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        OrderDTO order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đơn hàng thành công", order));
    }

    @PutMapping("/orders/{id}/info")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderInfo(
            @PathVariable Long id,
            @RequestBody OrderRequest request) {
        OrderDTO order = orderService.updateOrderInfo(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin đơn hàng thành công", order));
    }

    // ==================== User Management ====================
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserDTO> users = userService.getAllUsers(
                org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getId().equals(id)) {
            throw new BadRequestException("Bạn không thể tự khóa tài khoản của chính mình");
        }
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái tài khoản thành công", null));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getId().equals(id)) {
            throw new BadRequestException("Bạn không thể tự xóa tài khoản của chính mình");
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tài khoản thành công", null));
    }

    @GetMapping("/support-customers")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getSupportCustomers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getSupportCustomers()));
    }

    // ==================== Review Management ====================
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReviewDTO> reviews = reviewService.getAllReviews(page, size);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đánh giá thành công", null));
    }

    @PostMapping("/reviews/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleReviewStatus(@PathVariable Long id) {
        reviewService.toggleReviewStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đánh giá thành công", null));
    }

    // ==================== Banner Management ====================
    @GetMapping("/banners")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getAllBanners() {
        List<BannerDTO> banners = bannerService.getAllBanners();
        return ResponseEntity.ok(ApiResponse.success(banners));
    }

    @PostMapping("/banners")
    public ResponseEntity<ApiResponse<BannerDTO>> createBanner(
            @RequestPart("banner") BannerDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "subImage", required = false) MultipartFile subImage) throws IOException {
        BannerDTO banner = bannerService.createBanner(request, image, subImage);
        return ResponseEntity.ok(ApiResponse.success("Thêm banner thành công", banner));
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<ApiResponse<BannerDTO>> updateBanner(
            @PathVariable Long id,
            @RequestPart("banner") BannerDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "subImage", required = false) MultipartFile subImage) throws IOException {
        BannerDTO banner = bannerService.updateBanner(id, request, image, subImage);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật banner thành công", banner));
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa banner thành công", null));
    }

    // ==================== Coupon Management ====================
    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<Page<CouponDTO>>> listCoupons(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CouponDTO> coupons = couponService.list(keyword, active, page, size);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @GetMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<CouponDTO>> getCoupon(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getById(id)));
    }

    @PostMapping("/coupons")
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponDTO coupon = couponService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo mã giảm giá thành công", coupon));
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<CouponDTO>> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {
        CouponDTO coupon = couponService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật mã giảm giá thành công", coupon));
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã vô hiệu hóa mã giảm giá", null));
    }
}
