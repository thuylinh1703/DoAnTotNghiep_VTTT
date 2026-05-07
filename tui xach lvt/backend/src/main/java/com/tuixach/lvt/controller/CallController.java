package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.CallMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CallController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/call")
    public void handleCall(@Payload CallMessage message) {
        log.info("Received call message: {} from {} to {}", message.getType(), message.getFrom(), message.getTo());
        
        // Forward the message to the specific target topic
        // We use /topic/call/{email} so the receiver can subscribe to it
        messagingTemplate.convertAndSend("/topic/call/" + message.getTo(), message);
    }
}
