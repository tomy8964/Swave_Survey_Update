package com.example.surveyanswer.survey.repository.surveyAnswer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class SurveyAnswerRepositoryTest {

    @Autowired
    SurveyAnswerRepository surveyAnswerRepository;

    @Test
    void findSurveyAnswersBySurveyDocumentId() {
        surveyAnswerRepository.findSurveyAnswersBySurveyDocumentId(1L);
    }
}