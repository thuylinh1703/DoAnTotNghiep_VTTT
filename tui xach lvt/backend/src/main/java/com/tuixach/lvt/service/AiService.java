package com.tuixach.lvt.service;

import com.tuixach.lvt.entity.Product;
import com.tuixach.lvt.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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";

    public String getProductSuggestion(String keyword) {
        String context = getStoreContext();
        String prompt = String.format(
                "You are an expert sales assistant for 'LVT Handbag Store'. " +
                "Based on the keyword: '%s', suggest 3 best products. " +
                "Format as JSON array with 'id', 'name', 'reason'. Vietnamese language. " +
                "Products:\n%s",
                keyword, context
        );
        return callOpenRouter(prompt, true);
    }

    public String chat(String userMessage) {
        String context = getStoreContext();
        String prompt = String.format(
                "You are 'LVT AI', a friendly luxury handbag assistant. " +
                "Answer the user. If you recommend a product, MUST use the exact format: [item:id:product_name] " +
                "so the system can display a clickable link. (e.g., 'Bạn thử xem [item:297:Túi Gucci]'...).\n" +
                "Keep answers helpful and in Vietnamese. Tone: Premium.\n" +
                "Store Context:\n%s\n\nUser: %s",
                context, userMessage
        );
        return callOpenRouter(prompt, false);
    }

    private String getStoreContext() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .filter(Product::isActive)
                .limit(40) // Limit context to 40 products to avoid massive payloads
                .map(p -> String.format("[%d] %s - %s₫ (Brand: %s)", 
                        p.getId(), p.getName(), p.getPrice(), p.getBrand()))
                .collect(Collectors.joining("\n"));
    }

    @SuppressWarnings("unchecked")
    private String callOpenRouter(String prompt, boolean isJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.set("HTTP-Referer", "http://localhost:4200");
        headers.set("X-Title", "LVT-App");

        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek/deepseek-chat");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        body.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(
                OPENROUTER_API_URL, request, Map.class);
            
            Map<String, Object> response = responseEntity.getBody();
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    if (isJson) {
                        return content.replaceAll("```json", "").replaceAll("```", "").trim();
                    }
                    return content;
                }
            }
        } catch (Exception e) {
            log.error("Error calling OpenRouter: {}", e.getMessage());
            return isJson ? "[]" : "Xin lỗi, tôi đang gặp chút sự cố kết nối. Bạn vui lòng thử lại sau nhé!";
        }
        return isJson ? "[]" : "Tôi không tìm thấy câu trả lời phù hợp.";
    }
}
