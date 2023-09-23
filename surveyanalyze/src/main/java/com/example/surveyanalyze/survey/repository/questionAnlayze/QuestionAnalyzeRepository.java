package com.example.surveyanalyze.survey.repository.questionAnlayze;

import com.example.surveyanalyze.survey.domain.QuestionAnalyze;
import com.example.surveyanalyze.survey.domain.SurveyAnalyze;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionAnalyzeRepository extends JpaRepository<QuestionAnalyze, Long>, QuestionAnalyzeRepositoryCustom {

    @Transactional
    void deleteAllBySurveyAnalyzeId(SurveyAnalyze id);
}