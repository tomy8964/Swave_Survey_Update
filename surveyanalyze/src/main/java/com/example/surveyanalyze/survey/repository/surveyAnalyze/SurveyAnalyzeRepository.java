package com.example.surveyanalyze.survey.repository.surveyAnalyze;

import com.example.surveyanalyze.survey.domain.SurveyAnalyze;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface SurveyAnalyzeRepository extends JpaRepository<SurveyAnalyze, Long>, SurveyAnalyzeRepositoryCustom {
    @Transactional(readOnly = true)
    Optional<SurveyAnalyze> findBySurveyDocumentId(Long surveyDocumentId);

    @Transactional
    void deleteAllBySurveyDocumentId(Long surveyDocumentId);
}