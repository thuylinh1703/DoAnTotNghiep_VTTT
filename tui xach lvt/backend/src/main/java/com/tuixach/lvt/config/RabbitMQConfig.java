package com.tuixach.lvt.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE_NAME = "tuixach.exchange";

    // Queues
    public static final String QUEUE_REGISTRATION_OTP = "tuixach.queue.registration-otp";
    public static final String QUEUE_ORDER_CONFIRMATION = "tuixach.queue.order-confirmation";
    public static final String QUEUE_ORDER_COMPLETED = "tuixach.queue.order-completed";

    // Routing keys
    public static final String ROUTING_KEY_REGISTRATION_OTP = "tuixach.routing.registration-otp";
    public static final String ROUTING_KEY_ORDER_CONFIRMATION = "tuixach.routing.order-confirmation";
    public static final String ROUTING_KEY_ORDER_COMPLETED = "tuixach.routing.order-completed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // ===== Registration OTP Queue =====
    @Bean
    public Queue registrationOtpQueue() {
        return QueueBuilder.durable(QUEUE_REGISTRATION_OTP).build();
    }

    @Bean
    public Binding registrationOtpBinding(Queue registrationOtpQueue, TopicExchange exchange) {
        return BindingBuilder.bind(registrationOtpQueue).to(exchange).with(ROUTING_KEY_REGISTRATION_OTP);
    }

    // ===== Order Confirmation Queue =====
    @Bean
    public Queue orderConfirmationQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_CONFIRMATION).build();
    }

    @Bean
    public Binding orderConfirmationBinding(Queue orderConfirmationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderConfirmationQueue).to(exchange).with(ROUTING_KEY_ORDER_CONFIRMATION);
    }

    // ===== Order Completed (Analytics) Queue =====
    @Bean
    public Queue orderCompletedQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_COMPLETED).build();
    }

    @Bean
    public Binding orderCompletedBinding(Queue orderCompletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderCompletedQueue).to(exchange).with(ROUTING_KEY_ORDER_COMPLETED);
    }

    // ===== Message Converter (JSON) =====
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
