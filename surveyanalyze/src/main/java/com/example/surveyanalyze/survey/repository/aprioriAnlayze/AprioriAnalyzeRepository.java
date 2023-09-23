package com.example.surveyanalyze.survey.repository.aprioriAnlayze;

import com.example.surveyanalyze.survey.domain.AprioriAnalyze;
import com.example.surveyanalyze.survey.domain.SurveyAnalyze;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AprioriAnalyzeRepository extends JpaRepository<AprioriAnalyze, Long>, AprioriAnalyzeRepositoryCustom {

    @Transactional
    void deleteAllBySurveyAnalyzeId(SurveyAnalyze id);
}