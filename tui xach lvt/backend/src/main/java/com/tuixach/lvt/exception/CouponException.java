package com.tuixach.lvt.exception;

import lombok.Getter;

@Getter
public class CouponException extends RuntimeException {

    public enum Code {
        COUPON_NOT_FOUND,
        COUPON_INACTIVE,
        COUPON_NOT_STARTED,
        COUPON_EXPIRED,
        COUPON_USAGE_LIMIT_REACHED,
        COUPON_USER_LIMIT_REACHED,
        COUPON_MIN_ORDER_NOT_MET,
        COUPON_NOT_APPLICABLE
    }

    private final Code code;

    public CouponException(Code code, String message) {
        super(message);
        this.code = code;
    }
}
