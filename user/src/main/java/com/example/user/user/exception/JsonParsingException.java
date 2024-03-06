package com.example.user.user.exception;

public class JsonParsingException extends RuntimeException {
    private static final String MESSAGE = "잘못된 JSON 형식입니다.";

    public JsonParsingException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
