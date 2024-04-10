package com.example.surveydocument.survey.service;

import com.example.surveydocument.survey.request.DateDto;
import com.example.surveydocument.survey.request.PageRequestDto;
import com.example.surveydocument.survey.request.SurveyRequestDto;
import com.example.surveydocument.survey.response.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

public interface SurveyDocumentService {
    Long createSurvey(HttpServletRequest request, SurveyRequestDto surveyRequest);

    Page<SurveyPageDto> readSurveyList(HttpServletRequest request1, PageRequestDto request2);

    Long countChoice(Long choiceId);

    Long countSurveyDocument(Long surveyDocumentId);

    SurveyDetailDto readSurveyDetail(Long surveyDocumentId);

    ChoiceDetailDto getChoice(Long choiceId);

    QuestionDetailDto getQuestion(Long questionId);

    QuestionDetailDto getQuestionByChoiceId(Long choiceId);

    Long updateSurvey(HttpServletRequest request, SurveyRequestDto requestDto, Long surveyId);

    Long deleteSurvey(HttpServletRequest request, Long surveyId);

    Long updateDate(Long surveyDocumentId, DateDto request);

    Boolean managementEnable(Long id, Boolean enable);

    ManagementResponseDto managementSurvey(Long id);

    SurveyDetailDto2 readSurveyDetail2(Long surveyDocumentId);
}
