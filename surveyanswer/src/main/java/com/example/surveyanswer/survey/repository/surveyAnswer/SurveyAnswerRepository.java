package com.example.surveyanswer.survey.repository.surveyAnswer;

import com.example.surveyanswer.survey.domain.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long>, SurveyAnswerRepositoryCustom {
    List<SurveyAnswer> findSurveyAnswersBySurveyDocumentId(Long surveyId);
}
