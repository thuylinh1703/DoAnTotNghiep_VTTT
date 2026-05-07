package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.CouponDTO;
import com.tuixach.lvt.dto.CouponRequest;
import com.tuixach.lvt.dto.CouponValidateResponse;
import com.tuixach.lvt.entity.CartItem;
import com.tuixach.lvt.entity.Category;
import com.tuixach.lvt.entity.Coupon;
import com.tuixach.lvt.entity.CouponUsage;
import com.tuixach.lvt.entity.Order;
import com.tuixach.lvt.entity.Product;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.exception.CouponException;
import com.tuixach.lvt.exception.ResourceNotFoundException;
import com.tuixach.lvt.repository.CategoryRepository;
import com.tuixach.lvt.repository.CouponRepository;
import com.tuixach.lvt.repository.CouponUsageRepository;
import com.tuixach.lvt.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * Stateless validation — does NOT mutate usage count. Safe for cart preview.
     * The caller passes the actual cart (trusted server-side source) to avoid
     * letting clients probe arbitrary product IDs.
     */
    @Transactional(readOnly = true)
    public CouponValidateResponse validate(String rawCode, User user, List<CartItem> cartItems) {
        Coupon coupon = loadAndVerifyCoupon(rawCode, user);
        BigDecimal eligibleSubtotal = computeEligibleSubtotal(coupon, cartItems);
        enforceMinOrder(coupon, eligibleSubtotal);
        BigDecimal discount = computeDiscount(coupon, eligibleSubtotal);
        BigDecimal cartTotal = cartItems.stream()
                .map(CouponService::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal finalTotal = cartTotal.subtract(discount).max(BigDecimal.ZERO);

        return CouponValidateResponse.builder()
                .code(coupon.getCode())
                .type(coupon.getType().name())
                .value(coupon.getValue())
                .eligibleSubtotal(eligibleSubtotal)
                .discountAmount(discount)
                .finalTotal(finalTotal)
                .description(coupon.getDescription())
                .build();
    }

    /**
     * Atomic apply — called from OrderService.createOrder inside the same
     * transaction as stock decrement. Increments usedCount guarded by usageLimit;
     * throws COUPON_USAGE_LIMIT_REACHED if exhausted between validate and apply.
     * <p>
     * Returns the computed discount so OrderService can persist it on the order.
     */
    @Transactional
    public BigDecimal applyToOrder(String rawCode, User user, List<CartItem> cartItems, Order order) {
        Coupon coupon = loadAndVerifyCoupon(rawCode, user);
        BigDecimal eligibleSubtotal = computeEligibleSubtotal(coupon, cartItems);
        enforceMinOrder(coupon, eligibleSubtotal);
        BigDecimal discount = computeDiscount(coupon, eligibleSubtotal);

        if (coupon.getUsageLimit() != null) {
            int updated = couponRepository.incrementUsedCountIfAvailable(coupon.getId());
            if (updated == 0) {
                throw new CouponException(
                        CouponException.Code.COUPON_USAGE_LIMIT_REACHED,
                        "Mã giảm giá đã hết lượt sử dụng");
            }
        } else {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        }

        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .user(user)
                .order(order)
                .discountAmount(discount)
                .build();
        couponUsageRepository.save(usage);

        return discount;
    }

    private Coupon loadAndVerifyCoupon(String rawCode, User user) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new CouponException(CouponException.Code.COUPON_NOT_FOUND,
                    "Mã giảm giá không hợp lệ");
        }
        Coupon coupon = couponRepository.findByCodeIgnoreCase(rawCode.trim())
                .orElseThrow(() -> new CouponException(CouponException.Code.COUPON_NOT_FOUND,
                        "Mã giảm giá không tồn tại"));

        if (!coupon.isActive()) {
            throw new CouponException(CouponException.Code.COUPON_INACTIVE,
                    "Mã giảm giá đã bị vô hiệu hóa");
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            throw new CouponException(CouponException.Code.COUPON_NOT_STARTED,
                    "Mã giảm giá chưa có hiệu lực");
        }
        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            throw new CouponException(CouponException.Code.COUPON_EXPIRED,
                    "Mã giảm giá đã hết hạn");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new CouponException(CouponException.Code.COUPON_USAGE_LIMIT_REACHED,
                    "Mã giảm giá đã hết lượt sử dụng");
        }

        if (coupon.getPerUserLimit() != null && coupon.getPerUserLimit() > 0 && user != null) {
            long userUsage = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), user.getId());
            if (userUsage >= coupon.getPerUserLimit()) {
                throw new CouponException(CouponException.Code.COUPON_USER_LIMIT_REACHED,
                        "Bạn đã sử dụng mã này quá số lần cho phép");
            }
        }

        return coupon;
    }

    private void enforceMinOrder(Coupon coupon, BigDecimal eligibleSubtotal) {
        if (eligibleSubtotal.signum() <= 0) {
            throw new CouponException(CouponException.Code.COUPON_NOT_APPLICABLE,
                    "Không có sản phẩm nào trong giỏ được áp dụng mã này");
        }
        if (coupon.getMinOrderAmount() != null
                && eligibleSubtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new CouponException(CouponException.Code.COUPON_MIN_ORDER_NOT_MET,
                    "Đơn hàng tối thiểu " + coupon.getMinOrderAmount().toPlainString() + "₫ để áp dụng mã này");
        }
    }

    private BigDecimal computeEligibleSubtotal(Coupon coupon, List<CartItem> cartItems) {
        Set<Long> categoryScope = coupon.getCategories().stream()
                .map(Category::getId).collect(Collectors.toCollection(HashSet::new));
        Set<Long> productScope = coupon.getProducts().stream()
                .map(Product::getId).collect(Collectors.toCollection(HashSet::new));
        boolean scoped = !categoryScope.isEmpty() || !productScope.isEmpty();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Product p = item.getProduct();
            if (p == null || !p.isActive()) continue;
            if (scoped) {
                boolean categoryMatch = !categoryScope.isEmpty()
                        && p.getCategory() != null
                        && categoryScope.contains(p.getCategory().getId());
                boolean productMatch = !productScope.isEmpty()
                        && productScope.contains(p.getId());
                if (!categoryMatch && !productMatch) continue;
            }
            subtotal = subtotal.add(lineTotal(item));
        }
        return subtotal;
    }

    private BigDecimal computeDiscount(Coupon coupon, BigDecimal eligibleSubtotal) {
        BigDecimal discount;
        if (coupon.getType() == Coupon.CouponType.PERCENTAGE) {
            discount = eligibleSubtotal
                    .multiply(coupon.getValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null) {
                discount = discount.min(coupon.getMaxDiscountAmount());
            }
        } else {
            discount = coupon.getValue().min(eligibleSubtotal);
        }
        return discount.max(BigDecimal.ZERO);
    }

    private static BigDecimal lineTotal(CartItem item) {
        Product p = item.getProduct();
        if (p == null) return BigDecimal.ZERO;
        BigDecimal unit = p.getDiscountPrice() != null ? p.getDiscountPrice() : p.getPrice();
        return unit.multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    @Transactional(readOnly = true)
    public List<CouponDTO> getAvailableCoupons() {
        return couponRepository.findAllAvailable()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Admin CRUD ====================

    public Page<CouponDTO> list(String keyword, Boolean active, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return couponRepository.search(
                (keyword == null || keyword.isBlank()) ? null : keyword.trim(),
                active,
                pageable
        ).map(this::mapToDTO);
    }

    public CouponDTO getById(Long id) {
        return mapToDTO(findOrThrow(id));
    }

    @Transactional
    public CouponDTO create(CouponRequest request) {
        String normalized = request.getCode().trim().toUpperCase();
        if (couponRepository.existsByCodeIgnoreCase(normalized)) {
            throw new CouponException(CouponException.Code.COUPON_NOT_APPLICABLE,
                    "Mã đã tồn tại: " + normalized);
        }
        Coupon.CouponType type = parseType(request.getType());
        validateDateRange(request.getStartDate(), request.getEndDate());
        if (type == Coupon.CouponType.PERCENTAGE
                && request.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new CouponException(CouponException.Code.COUPON_NOT_APPLICABLE,
                    "Giảm giá phần trăm không thể vượt quá 100");
        }

        Coupon coupon = Coupon.builder()
                .code(normalized)
                .description(request.getDescription())
                .type(type)
                .value(request.getValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .perUserLimit(request.getPerUserLimit() != null ? request.getPerUserLimit() : 1)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.getActive() == null || request.getActive())
                .categories(resolveCategories(request.getCategoryIds()))
                .products(resolveProducts(request.getProductIds()))
                .build();

        couponRepository.save(coupon);
        return mapToDTO(coupon);
    }

    @Transactional
    public CouponDTO update(Long id, CouponRequest request) {
        Coupon coupon = findOrThrow(id);
        String normalized = request.getCode().trim().toUpperCase();
        if (!coupon.getCode().equalsIgnoreCase(normalized)
                && couponRepository.existsByCodeIgnoreCase(normalized)) {
            throw new CouponException(CouponException.Code.COUPON_NOT_APPLICABLE,
                    "Mã đã tồn tại: " + normalized);
        }
        Coupon.CouponType type = parseType(request.getType());
        validateDateRange(request.getStartDate(), request.getEndDate());

        coupon.setCode(normalized);
        coupon.setDescription(request.getDescription());
        coupon.setType(type);
        coupon.setValue(request.getValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setPerUserLimit(request.getPerUserLimit() != null ? request.getPerUserLimit() : 1);
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        if (request.getActive() != null) coupon.setActive(request.getActive());
        coupon.setCategories(resolveCategories(request.getCategoryIds()));
        coupon.setProducts(resolveProducts(request.getProductIds()));

        couponRepository.save(coupon);
        return mapToDTO(coupon);
    }

    @Transactional
    public void delete(Long id) {
        Coupon coupon = findOrThrow(id);
        coupon.setActive(false);
        couponRepository.save(coupon);
    }

    private Coupon findOrThrow(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá"));
    }

    private Coupon.CouponType parseType(String raw) {
        try {
            return Coupon.CouponType.valueOf(raw);
        } catch (Exception e) {
            throw new CouponException(CouponException.Code.COUPON_NOT_APPLICABLE,
                    "Loại giảm giá không hợp lệ");
        }
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new CouponException(CouponException.Code.COUPON_NOT_APPLICABLE,
                    "Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private Set<Category> resolveCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(categoryRepository.findAllById(ids));
    }

    private Set<Product> resolveProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(productRepository.findAllById(ids));
    }

    public CouponDTO mapToDTO(Coupon c) {
        return CouponDTO.builder()
                .id(c.getId())
                .code(c.getCode())
                .description(c.getDescription())
                .type(c.getType().name())
                .value(c.getValue())
                .minOrderAmount(c.getMinOrderAmount())
                .maxDiscountAmount(c.getMaxDiscountAmount())
                .usageLimit(c.getUsageLimit())
                .usedCount(c.getUsedCount())
                .perUserLimit(c.getPerUserLimit())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .active(c.isActive())
                .categoryIds(c.getCategories().stream().map(Category::getId).collect(Collectors.toList()))
                .productIds(c.getProducts().stream().map(Product::getId).collect(Collectors.toList()))
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
