package com.example.surveydocument.survey.repository.survey;

import com.example.surveydocument.survey.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long>, SurveyRepositoryCustom {
    Survey findByUserCode(Long userCode);
}