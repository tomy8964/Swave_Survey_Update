package com.example.surveydocument.survey.exception;

public class InterServerException extends RuntimeException {
    public InterServerException(Exception e) {
        super("내부 통신 오류입니다.\n" + e.getMessage());
    }
}
