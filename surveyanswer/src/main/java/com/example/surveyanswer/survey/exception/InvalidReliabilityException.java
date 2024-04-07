package com.example.surveyanswer.survey.exception;

public class InvalidReliabilityException extends RuntimeException{
    private static final String MESSAGE = "진정성 검사에 실패한 응답입니다.";

    public InvalidReliabilityException() {
        super(MESSAGE);
    }}
