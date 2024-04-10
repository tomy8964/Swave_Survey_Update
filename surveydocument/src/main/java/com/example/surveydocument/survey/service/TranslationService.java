package com.example.surveydocument.survey.service;

import com.example.surveydocument.survey.domain.*;
import com.example.surveydocument.survey.request.*;
import com.example.surveydocument.survey.response.*;

public interface TranslationService {
    QuestionDetailDto entityToDto(QuestionDocument questionDocument);

    ChoiceDetailDto entityToDto(Choice choice);

    WordCloudDto entityToDto(WordCloud wordCloud);

    DesignResponseDto entityToDto(Design design);

    ManagementResponseDto entityToDto(DateManagement dateManagement);

    SurveyDetailDto entityToDto1(SurveyDocument surveyDocument);

    SurveyDetailDto2 entityToDto2(SurveyDocument surveyDocument);

    Choice DtoToEntity(ChoiceRequestDto choiceRequestDto, QuestionDocument questionDocument);

    QuestionDocument DtoToEntity(QuestionRequestDto questionRequestDto, SurveyDocument surveyDocument);

    DateManagement DtoToEntity(DateDto dateDto, SurveyDocument surveyDocument);

    Design DtoToEntity(DesignRequestDto designRequestDto, SurveyDocument surveyDocument);

    SurveyDocument DtoToEntity(SurveyRequestDto surveyRequestDto, Long userId);

}
