package com.example.user.user.exception;

public class UnKnownProviderException extends RuntimeException {
    private static final String MESSAGE = "알 수 없는 제공자입니다.";

    public UnKnownProviderException(Throwable cause) {
        super(MESSAGE, cause);
    }

    public UnKnownProviderException() {
        super(MESSAGE);
    }
}
