package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.CartDTO;
import com.tuixach.lvt.dto.CartItemDTO;
import com.tuixach.lvt.dto.CartItemRequest;
import com.tuixach.lvt.dto.CartStockValidationDTO;
import com.tuixach.lvt.entity.CartItem;
import com.tuixach.lvt.entity.Product;
import com.tuixach.lvt.entity.ProductImage;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.exception.BadRequestException;
import com.tuixach.lvt.exception.InsufficientStockException;
import com.tuixach.lvt.exception.ResourceNotFoundException;
import com.tuixach.lvt.repository.CartItemRepository;
import com.tuixach.lvt.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartDTO getCart(User user) {
        List<CartItem> items = cartItemRepository.findByUserId(user.getId());
        List<CartItemDTO> itemDTOs = items.stream().map(this::mapToDTO).collect(Collectors.toList());

        BigDecimal totalAmount = itemDTOs.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDTO.builder()
                .items(itemDTOs)
                .totalAmount(totalAmount)
                .totalItems(itemDTOs.size())
                .build();
    }

    @Transactional
    public CartDTO addToCart(User user, CartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        if (!product.isActive()) {
            throw new BadRequestException("Sản phẩm không khả dụng");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId());
        int resultingQuantity = existingItem.map(CartItem::getQuantity).orElse(0) + request.getQuantity();

        if (product.getQuantity() < resultingQuantity) {
            throw new InsufficientStockException(product.getId(), product.getName(), product.getQuantity());
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(resultingQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }

        return getCart(user);
    }

    @Transactional
    public CartDTO updateCartItemQuantity(User user, Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền thao tác");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            Product product = cartItem.getProduct();
            if (product.getQuantity() < quantity) {
                throw new InsufficientStockException(product.getId(), product.getName(), product.getQuantity());
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return getCart(user);
    }

    @Transactional
    public CartDTO removeFromCart(User user, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền thao tác");
        }

        cartItemRepository.delete(cartItem);
        return getCart(user);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUserId(user.getId());
    }

    /**
     * Advisory, non-locking stock scan of the user's cart. Used by the frontend
     * right before checkout to surface "chỉ còn X trong kho" issues. This is NOT
     * a reservation — source of truth is the locked check inside OrderService.
     */
    public CartStockValidationDTO validateStock(User user) {
        List<CartItem> items = cartItemRepository.findByUserId(user.getId());
        List<CartStockValidationDTO.Issue> issues = new ArrayList<>();
        for (CartItem item : items) {
            Product product = item.getProduct();
            if (!product.isActive() || product.getQuantity() < item.getQuantity()) {
                issues.add(CartStockValidationDTO.Issue.builder()
                        .cartItemId(item.getId())
                        .productId(product.getId())
                        .productName(product.getName())
                        .requested(item.getQuantity())
                        .available(product.isActive() ? product.getQuantity() : 0)
                        .build());
            }
        }
        return CartStockValidationDTO.builder()
                .allAvailable(issues.isEmpty())
                .issues(issues)
                .build();
    }

    private CartItemDTO mapToDTO(CartItem item) {
        Product product = item.getProduct();
        BigDecimal effectivePrice = product.getDiscountPrice() != null ? product.getDiscountPrice()
                : product.getPrice();

        String primaryImage = product.getImages() != null
                ? product.getImages().stream()
                        .filter(ProductImage::isPrimary)
                        .map(ProductImage::getImageUrl)
                        .findFirst()
                        .orElse(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl())
                : null;

        return CartItemDTO.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImage(primaryImage)
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .brand(product.getBrand())
                .quantity(item.getQuantity())
                .subtotal(effectivePrice.multiply(BigDecimal.valueOf(item.getQuantity())))
                .stock(product.isActive() ? product.getQuantity() : 0)
                .build();
    }
}
