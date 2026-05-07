package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.ApiResponse;
import com.tuixach.lvt.dto.CouponValidateRequest;
import com.tuixach.lvt.dto.CouponValidateResponse;
import com.tuixach.lvt.entity.CartItem;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.repository.CartItemRepository;
import com.tuixach.lvt.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CartItemRepository cartItemRepository;

    /**
     * Validates a coupon against the authenticated user's current cart.
     * Intentionally does not accept cart contents from the client — prevents
     * attackers from probing stock/discount behavior against arbitrary products.
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<CouponValidateResponse>> validate(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CouponValidateRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        CouponValidateResponse response = couponService.validate(request.getCode(), user, cartItems);
        return ResponseEntity.ok(ApiResponse.success("Áp dụng mã thành công", response));
    }
}
