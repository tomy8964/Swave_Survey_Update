package com.example.surveyanalyze.survey.repository.choiceAnalyze;

import com.example.surveyanalyze.survey.domain.ChoiceAnalyze;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoiceAnalyzeRepository extends JpaRepository<ChoiceAnalyze, Long>, ChoiceAnalyzeRepositoryCustom {
}