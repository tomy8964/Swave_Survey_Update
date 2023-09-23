package com.example.surveyanalyze.survey.repository.chiAnlayze;

import com.example.surveyanalyze.survey.domain.ChiAnalyze;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChiAnalyzeRepository extends JpaRepository<ChiAnalyze, Long>, ChiAnalyzeRepositoryCustom {

}