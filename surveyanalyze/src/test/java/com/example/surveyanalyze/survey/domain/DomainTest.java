package com.example.surveyanalyze.survey.domain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DomainTest {

    @Test
    public void domainTest() {
        AprioriAnalyze.builder()
                .surveyAnalyze(null)
                .build();
        ChiAnalyze.builder()
                .questionAnalyze(null)
                .build();
        ChoiceAnalyze.builder()
                .aprioriAnalyze(null)
                .build();
        CompareAnalyze.builder()
                .questionAnalyze(null)
                .build();
        QuestionAnalyze.builder()
                .surveyAnalyze(null)
                .build();
    }

}