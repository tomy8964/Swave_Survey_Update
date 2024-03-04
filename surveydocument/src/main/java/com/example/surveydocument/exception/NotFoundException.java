package com.example.surveydocument.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super("존재하지 않는 " + message + "입니다.");
    }
}
