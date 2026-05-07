package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.OrderConfirmationMessage;
import com.tuixach.lvt.dto.OrderDTO;
import com.tuixach.lvt.dto.OrderItemDTO;
import com.tuixach.lvt.dto.OrderRequest;
import com.tuixach.lvt.entity.*;
import com.tuixach.lvt.exception.BadRequestException;
import com.tuixach.lvt.exception.InsufficientStockException;
import com.tuixach.lvt.exception.ResourceNotFoundException;
import com.tuixach.lvt.repository.CartItemRepository;
import com.tuixach.lvt.repository.OrderRepository;
import com.tuixach.lvt.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    // private final RabbitMQProducer rabbitMQProducer;
    private final CouponService couponService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderDTO createOrder(User user, OrderRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }

        Order.PaymentMethod paymentMethod;
        try {
            paymentMethod = Order.PaymentMethod.valueOf(request.getPaymentMethod());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Phương thức thanh toán không hợp lệ");
        }

        // Deadlock prevention: lock products in a consistent global order (ascending productId).
        // Two concurrent carts with overlapping products would otherwise deadlock (A→B vs B→A).
        List<CartItem> sortedItems = cartItems.stream()
                .sorted(Comparator.comparing(ci -> ci.getProduct().getId()))
                .toList();

        String orderCode = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .paymentMethod(paymentMethod)
                .status(Order.OrderStatus.PENDING)
                .note(request.getNote())
                .stockRestored(false)
                .discountAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : sortedItems) {
            Long productId = cartItem.getProduct().getId();
            int requested = cartItem.getQuantity();

            // SELECT ... FOR UPDATE — blocks other transactions on this row until commit.
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

            if (!product.isActive()) {
                throw new BadRequestException("Sản phẩm '" + product.getName() + "' không còn khả dụng");
            }

            if (product.getQuantity() < requested) {
                throw new InsufficientStockException(product.getId(), product.getName(), product.getQuantity());
            }

            BigDecimal effectivePrice = product.getDiscountPrice() != null
                    ? product.getDiscountPrice()
                    : product.getPrice();
            BigDecimal subtotal = effectivePrice.multiply(BigDecimal.valueOf(requested));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .price(effectivePrice)
                    .quantity(requested)
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(subtotal);

            product.setQuantity(product.getQuantity() - requested);
            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);

        // Coupon apply happens INSIDE the order transaction so stock decrement and
        // coupon-usage increment either both commit or both roll back. If the
        // coupon is exhausted between client-side validate and this call, the
        // atomic UPDATE in CouponService returns 0 and throws — rollback releases
        // the stock we just decremented.
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            // Save first to give the order a managed identity for CouponUsage FK.
            orderRepository.save(order);
            BigDecimal discount = couponService.applyToOrder(
                    request.getCouponCode(), user, sortedItems, order);
            order.setCouponCode(request.getCouponCode().trim().toUpperCase());
            order.setDiscountAmount(discount);
        }

        orderRepository.save(order);

        cartItemRepository.deleteByUserId(user.getId());

        sendOrderConfirmationEmail(user, order);

        return mapToDTO(order);
    }

    public Page<OrderDTO> getUserOrders(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToDTO);
    }

    public OrderDTO getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        return mapToDTO(order);
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        return mapToDTO(order);
    }

    // Admin methods
    public Page<OrderDTO> getAllOrders(int page, int size, String status, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Order.OrderStatus orderStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                orderStatus = Order.OrderStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {}
        }
        
        return orderRepository.searchOrders(orderStatus, search, pageable).map(this::mapToDTO);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Trạng thái đơn hàng không hợp lệ");
        }

        // Prevent changing status if already COMPLETED or CANCELLED
        if (order.getStatus() == Order.OrderStatus.COMPLETED || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException("Không thể thay đổi trạng thái của đơn hàng đã hoàn thành hoặc đã hủy");
        }

        if (newStatus == Order.OrderStatus.CANCELLED && order.getStatus() != Order.OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        return mapToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderInfo(Long orderId, OrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể chỉnh sửa thông tin đơn hàng ở trạng thái CHỜ XỬ LÝ (PENDING)");
        }

        if (request.getReceiverName() != null) order.setReceiverName(request.getReceiverName());
        if (request.getReceiverPhone() != null) order.setReceiverPhone(request.getReceiverPhone());
        if (request.getReceiverAddress() != null) order.setReceiverAddress(request.getReceiverAddress());
        if (request.getNote() != null) order.setNote(request.getNote());

        orderRepository.save(order);
        return mapToDTO(order);
    }

    /**
     * Idempotent stock restore. Safe to call multiple times (e.g. from VNPay callback
     * retry or scheduled cleanup). Locks each product before incrementing to avoid
     * races with concurrent orders on the same SKU.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void restoreStock(Order order) {
        if (order.isStockRestored()) {
            return;
        }

        List<OrderItem> sortedItems = order.getItems().stream()
                .sorted(Comparator.comparing(oi -> oi.getProduct().getId()))
                .toList();

        for (OrderItem item : sortedItems) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStockRestored(true);
        orderRepository.save(order);
    }

    /**
     * Cancel a PENDING order and restore stock. Used by VNPay failure path and
     * abandoned-order cleanup job. Only cancels if still PENDING — prevents
     * double-cancel races.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean cancelIfPending(String orderCode, String reason) {
        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
        if (order == null || order.getStatus() != Order.OrderStatus.PENDING) {
            return false;
        }
        restoreStock(order);
        order.setStatus(Order.OrderStatus.CANCELLED);
        if (reason != null) {
            String existing = order.getNote() == null ? "" : order.getNote();
            order.setNote((existing.isBlank() ? "" : existing + " | ") + "[Tự động hủy] " + reason);
        }
        orderRepository.save(order);
        return true;
    }

    @Transactional
    public OrderDTO confirmReceived(User user, String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền thực hiện hành động này");
        }

        order.setStatus(Order.OrderStatus.COMPLETED);
        orderRepository.save(order);
        return mapToDTO(order);
    }

    public OrderDTO mapToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> {
                    String primaryImage = item.getProduct().getImages() != null
                        ? item.getProduct().getImages().stream()
                            .filter(ProductImage::isPrimary)
                            .map(ProductImage::getImageUrl)
                            .findFirst()
                            .orElse(item.getProduct().getImages().isEmpty() ? null : item.getProduct().getImages().get(0).getImageUrl())
                        : null;

                    return OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .productImageUrl(primaryImage)
                        .build();
                })
                .collect(Collectors.toList());

        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = order.getTotalAmount() != null
                ? order.getTotalAmount().subtract(discount).max(BigDecimal.ZERO)
                : BigDecimal.ZERO;

        return OrderDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .paymentMethod(order.getPaymentMethod().name())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .note(order.getNote())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .build();
    }

    /**
     * Gửi email xác nhận đơn hàng qua RabbitMQ
     */
    private void sendOrderConfirmationEmail(User user, Order order) {
        try {
            List<OrderConfirmationMessage.OrderItemInfo> itemInfos = order.getItems().stream()
                    .map(item -> OrderConfirmationMessage.OrderItemInfo.builder()
                            .productName(item.getProductName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .subtotal(item.getSubtotal())
                            .build())
                    .collect(Collectors.toList());

            OrderConfirmationMessage message = OrderConfirmationMessage.builder()
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .orderCode(order.getOrderCode())
                    .receiverName(order.getReceiverName())
                    .receiverPhone(order.getReceiverPhone())
                    .receiverAddress(order.getReceiverAddress())
                    .paymentMethod(order.getPaymentMethod().name())
                    .totalAmount(order.getTotalAmount())
                    .items(itemInfos)
                    .build();

            // rabbitMQProducer.sendOrderConfirmationMessage(message);
            log.info("RabbitMQ is disabled. Order confirmation skipped for order: {}", order.getOrderCode());
        } catch (Exception e) {
            // Log error nhưng không throw để không ảnh hưởng đến đặt hàng
            // Email sẽ được gửi bất đồng bộ
        }
    }

    @Transactional
    public OrderDTO updateOrderStatusByCode(String orderCode, String status) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        return updateOrderStatus(order.getId(), status);
    }
}
