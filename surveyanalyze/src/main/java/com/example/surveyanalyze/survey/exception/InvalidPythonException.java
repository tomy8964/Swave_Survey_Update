package com.example.surveyanalyze.survey.exception;

public class InvalidPythonException extends RuntimeException {
    private static final String MESSAGE = "python 입출력 혹은 올바르지 않은 python 파일입니다.";

    public InvalidPythonException() {
        super(MESSAGE);
    }

    public InvalidPythonException(Exception e) {
        super(MESSAGE, e);
    }
}

