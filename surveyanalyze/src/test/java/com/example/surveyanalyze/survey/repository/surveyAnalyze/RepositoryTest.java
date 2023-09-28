package com.example.surveyanalyze.survey.repository.surveyAnalyze;

import com.example.surveyanalyze.survey.domain.QuestionAnalyze;
import com.example.surveyanalyze.survey.domain.SurveyAnalyze;
import com.example.surveyanalyze.survey.repository.questionAnlayze.QuestionAnalyzeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RepositoryTest {

    @PersistenceContext
    EntityManager em;
    @Autowired
    SurveyAnalyzeRepository surveyAnalyzeRepository;
    @Autowired
    private QuestionAnalyzeRepository questionAnalyzeRepository;

    @Test
    void findBySurveyDocumentId() {
        surveyAnalyzeRepository.findBySurveyDocumentId(1L);
    }

    @Test
    public void deleteAllBySurveyDocumentId() {
        //given
        SurveyAnalyze surveyAnalyze = SurveyAnalyze.builder()
                .surveyDocumentId(-1L)
                .build();

        QuestionAnalyze questionAnalyze = QuestionAnalyze.builder()
                .surveyAnalyze(surveyAnalyze)
                .questionTitle("Question 1")
                .wordCloud("word cloud")
                .build();


        questionAnalyzeRepository.save(questionAnalyze);
        surveyAnalyzeRepository.save(surveyAnalyze);

        em.flush();
        em.clear();
        //when
        surveyAnalyzeRepository.deleteAllBySurveyDocumentId(-1L);
        List<SurveyAnalyze> surveyAnalyzeList = surveyAnalyzeRepository.findAll();
        List<QuestionAnalyze> questionAnalyzeList = questionAnalyzeRepository.findAll();

        //then
        assertEquals(surveyAnalyzeList.size(), questionAnalyzeList.size());
    }

}