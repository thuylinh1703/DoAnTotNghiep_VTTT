package com.tuixach.lvt.exception;

import lombok.Getter;

@Getter
public class ReviewException extends RuntimeException {

    private final Code code;

    public ReviewException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public enum Code {
        NOT_PURCHASED,
        ALREADY_REVIEWED,
        NOT_DELIVERED
    }
}
