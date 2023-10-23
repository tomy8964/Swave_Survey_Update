package com.example.surveyanswer.survey.repository.surveyAnswer;

import com.example.surveyanswer.survey.domain.SurveyAnswer;

import java.util.List;

public interface SurveyAnswerRepositoryCustom {
    List<SurveyAnswer> findSurveyAnswerListBySurveyDocumentId(Long surveyId);
}
