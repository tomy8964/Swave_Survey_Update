package com.example.surveyanalyze.survey.exception;

public class InvalidTokenException extends Exception {
    private static final String MESSAGE = "인가되지 않은 사용자입니다";

    public InvalidTokenException() {
        super(MESSAGE);
    }
}
