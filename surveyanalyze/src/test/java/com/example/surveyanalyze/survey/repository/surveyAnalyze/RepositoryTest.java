package com.example.surveyanalyze.survey.repository.surveyAnalyze;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RepositoryTest {

    @Autowired
    SurveyAnalyzeRepository surveyAnalyzeRepository;

    @Test
    void findBySurveyDocumentId() {
        surveyAnalyzeRepository.findBySurveyDocumentId(1L);
    }
}