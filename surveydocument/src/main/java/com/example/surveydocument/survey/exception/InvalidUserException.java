package com.example.surveydocument.survey.exception;

public class InvalidUserException extends RuntimeException {
    public InvalidUserException() {
        super("유저 정보가 올바르지 않습니다.");
    }
    public InvalidUserException(String message) {
        super(message);
    }
}
