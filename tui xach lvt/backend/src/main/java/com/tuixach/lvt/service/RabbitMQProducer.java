package com.tuixach.lvt.service;

import com.tuixach.lvt.config.RabbitMQConfig;
import com.tuixach.lvt.dto.OrderConfirmationMessage;
import com.tuixach.lvt.dto.RegistrationOtpMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

// @Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Gửi message OTP đăng ký vào queue
     */
    public void sendRegistrationOtpMessage(RegistrationOtpMessage message) {
        log.info("Sending registration OTP message to queue for email: {}", message.getEmail());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_REGISTRATION_OTP,
                message);
        log.info("Registration OTP message sent successfully for email: {}", message.getEmail());
    }

    /**
     * Gửi message xác nhận đơn hàng vào queue
     */
    public void sendOrderConfirmationMessage(OrderConfirmationMessage message) {
        log.info("Sending order confirmation message to queue for order: {}", message.getOrderCode());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_ORDER_CONFIRMATION,
                message);
        log.info("Order confirmation message sent successfully for order: {}", message.getOrderCode());
    }
}
