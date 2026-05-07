package com.tuixach.lvt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartStockValidationDTO {
    private boolean allAvailable;
    private List<Issue> issues;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {
        private Long cartItemId;
        private Long productId;
        private String productName;
        private int requested;
        private int available;
    }
}
