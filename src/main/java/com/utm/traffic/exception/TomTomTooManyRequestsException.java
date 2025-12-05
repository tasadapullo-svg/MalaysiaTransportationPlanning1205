package com.utm.traffic.exception;

/**
 * TomTom接口请求过于频繁异常（429）
 */
public class TomTomTooManyRequestsException extends RuntimeException {
    private int retryAfter; // 可选：接口返回的重试等待时间（秒）

    public TomTomTooManyRequestsException(String message) {
        super(message);
    }

    public TomTomTooManyRequestsException(String message, int retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    // getter
    public int getRetryAfter() {
        return retryAfter;
    }
}