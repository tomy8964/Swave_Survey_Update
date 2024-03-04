package com.example.surveydocument.exception;

public class InvalidUserException extends RuntimeException {
    private static final String MESSAGE = "유저 정보가 올바르지 않습니다.";

    public InvalidUserException() {
        super(MESSAGE);
    }

    public InvalidUserException(String message) {
        super(message);
    }
}
