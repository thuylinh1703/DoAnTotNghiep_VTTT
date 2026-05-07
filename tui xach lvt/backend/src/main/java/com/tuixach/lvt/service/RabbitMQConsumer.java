package com.tuixach.lvt.service;

import com.tuixach.lvt.config.RabbitMQConfig;
import com.tuixach.lvt.dto.OrderConfirmationMessage;
import com.tuixach.lvt.dto.RegistrationOtpMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

// @Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final EmailService emailService;

    /**
     * Lắng nghe queue OTP đăng ký → gửi email OTP
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_REGISTRATION_OTP)
    public void handleRegistrationOtp(RegistrationOtpMessage message) {
        log.info("Received registration OTP message for email: {}", message.getEmail());
        try {
            emailService.sendRegistrationOtpEmail(
                    message.getEmail(),
                    message.getFullName(),
                    message.getOtpCode());
            log.info("Registration OTP email sent successfully to: {}", message.getEmail());
        } catch (Exception e) {
            log.error("Failed to send registration OTP email to {}: {}", message.getEmail(), e.getMessage());
        }
    }

    /**
     * Lắng nghe queue xác nhận đơn hàng → gửi email xác nhận
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_CONFIRMATION)
    public void handleOrderConfirmation(OrderConfirmationMessage message) {
        log.info("Received order confirmation message for order: {}", message.getOrderCode());
        try {
            emailService.sendOrderConfirmationEmail(message);
            log.info("Order confirmation email sent successfully for order: {}", message.getOrderCode());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}", message.getOrderCode(),
                    e.getMessage());
        }
    }
}
