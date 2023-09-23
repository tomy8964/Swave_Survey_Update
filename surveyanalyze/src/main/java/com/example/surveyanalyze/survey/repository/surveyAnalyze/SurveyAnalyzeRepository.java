package com.example.surveyanalyze.survey.repository.surveyAnalyze;

import com.example.surveyanalyze.survey.domain.SurveyAnalyze;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyAnalyzeRepository extends JpaRepository<SurveyAnalyze, Long>, SurveyAnalyzeRepositoryCustom {
    SurveyAnalyze findBySurveyDocumentId(Long surveyDocumentId);
}