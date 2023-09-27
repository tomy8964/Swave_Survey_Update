package com.example.surveyanswer.survey.exception;

public class InvalidReliabilityException extends RuntimeException{
    private static final String MESSAGE = "회원 정보를 찾을 수 없습니다 ";

    public InvalidReliabilityException() {
        super(MESSAGE);
    }}
