package com.tuixach.lvt.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final String productName;
    private final int available;

    public InsufficientStockException(Long productId, String productName, int available) {
        super(buildMessage(productName, available));
        this.productId = productId;
        this.productName = productName;
        this.available = available;
    }

    private static String buildMessage(String productName, int available) {
        if (available <= 0) {
            return "Sản phẩm '" + productName + "' đã hết hàng";
        }
        return "Sản phẩm '" + productName + "' chỉ còn " + available + " trong kho";
    }
}
