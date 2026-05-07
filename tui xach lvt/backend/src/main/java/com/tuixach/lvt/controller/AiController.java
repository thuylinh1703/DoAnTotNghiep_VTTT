package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.ApiResponse;
import com.tuixach.lvt.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiController {

    private final AiService aiService;

    @GetMapping("/suggest")
    public ResponseEntity<String> getSuggestion(@RequestParam String keyword) {
        String result = aiService.getProductSuggestion(keyword);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody String message) {
        // Remove quotes if present from raw body
        String cleanMessage = message.replace("\"", "");
        String response = aiService.chat(cleanMessage);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
