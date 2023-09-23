package com.example.surveydocument.survey.exception;

public class InvalidProcessException extends RuntimeException {
    private static final String MESSAGE = "자바의 process bulider 오류입니다.";

    public InvalidProcessException() {
        super(MESSAGE);
    }
    public InvalidProcessException(Exception e) {
        super(MESSAGE,e);
    }
}

