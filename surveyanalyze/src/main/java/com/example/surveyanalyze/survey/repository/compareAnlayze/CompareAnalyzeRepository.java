package com.example.surveyanalyze.survey.repository.compareAnlayze;

import com.example.surveyanalyze.survey.domain.CompareAnalyze;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompareAnalyzeRepository extends JpaRepository<CompareAnalyze, Long>, CompareAnalyzeRepositoryCustom {

}