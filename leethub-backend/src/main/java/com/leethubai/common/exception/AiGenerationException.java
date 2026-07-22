package com.leethubai.common.exception;

public class AiGenerationException extends RuntimeException {

    private final String errorCode;

    public AiGenerationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AiGenerationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
