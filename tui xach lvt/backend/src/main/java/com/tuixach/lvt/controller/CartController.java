package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.*;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@AuthenticationPrincipal User user) {
        CartDTO cart = cartService.getCart(user);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartDTO>> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartItemRequest request) {
        CartDTO cart = cartService.addToCart(user, request);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm vào giỏ hàng", cart));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId,
            @RequestParam int quantity) {
        CartDTO cart = cartService.updateCartItemQuantity(user, cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật giỏ hàng thành công", cart));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId) {
        CartDTO cart = cartService.removeFromCart(user, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng", cart));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa toàn bộ giỏ hàng", null));
    }

    @PostMapping("/validate-stock")
    public ResponseEntity<ApiResponse<CartStockValidationDTO>> validateStock(
            @AuthenticationPrincipal User user) {
        CartStockValidationDTO result = cartService.validateStock(user);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
