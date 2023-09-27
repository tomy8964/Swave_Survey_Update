package com.example.surveyanswer.survey.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class QuestionAnswerTest {

    @Test
    @DisplayName("QuestionAnswer Builder test")
    void builder() {
        QuestionAnswer questionAnswer = new QuestionAnswer(1L, "", null, 0, "", 1L);
    }
}