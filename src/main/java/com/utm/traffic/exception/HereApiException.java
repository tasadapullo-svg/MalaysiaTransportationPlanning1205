package com.utm.traffic.exception;

/**
 * HERE API请求异常
 */
public class HereApiException extends RuntimeException {
    private int responseCode;
    private String responseContent;

    public HereApiException(int responseCode, String responseContent, String message) {
        super(message);
        this.responseCode = responseCode;
        this.responseContent = responseContent;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseContent() {
        return responseContent;
    }
}