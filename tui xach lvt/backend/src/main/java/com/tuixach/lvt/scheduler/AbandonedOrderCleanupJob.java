package com.tuixach.lvt.scheduler;

import com.tuixach.lvt.entity.Order;
import com.tuixach.lvt.repository.OrderRepository;
import com.tuixach.lvt.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VNPay gives the customer 15 minutes to complete payment. If they abandon the
 * tab, we never receive a return callback, so stock stays decremented forever.
 * This job cancels PENDING VNPAY orders older than 15 minutes and restores stock.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AbandonedOrderCleanupJob {

    private static final int ABANDON_TIMEOUT_MINUTES = 15;

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT2M")
    public void cleanupAbandonedVnpayOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(ABANDON_TIMEOUT_MINUTES);
        List<Order> abandoned = orderRepository.findAbandonedVnpayOrders(cutoff);
        if (abandoned.isEmpty()) {
            return;
        }
        log.info("Found {} abandoned VNPay PENDING orders (older than {} min)",
                abandoned.size(), ABANDON_TIMEOUT_MINUTES);
        for (Order order : abandoned) {
            try {
                orderService.cancelIfPending(order.getOrderCode(), "VNPay hết hạn thanh toán");
            } catch (Exception e) {
                log.error("Failed to cancel abandoned order {}: {}", order.getOrderCode(), e.getMessage());
            }
        }
    }
}
