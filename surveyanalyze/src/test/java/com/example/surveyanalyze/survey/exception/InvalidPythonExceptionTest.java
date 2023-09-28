package com.example.surveyanalyze.survey.exception;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InvalidPythonExceptionTest {

    @Test
    public void exception() {
        InvalidPythonException exception = new InvalidPythonException(new Exception());
        System.out.println("exception = " + exception);
    }

}