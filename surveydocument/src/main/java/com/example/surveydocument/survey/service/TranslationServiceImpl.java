package com.example.surveydocument.survey.service;

import com.example.surveydocument.restAPI.service.RestApiService;
import com.example.surveydocument.survey.domain.*;
import com.example.surveydocument.survey.request.*;
import com.example.surveydocument.survey.response.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {
    private final RestApiService apiService;

    @Override
    public QuestionDetailDto entityToDto(QuestionDocument questionDocument) {
        return QuestionDetailDto.builder()
                .id(questionDocument.getId())
                .title(questionDocument.getTitle())
                .questionType(questionDocument.getQuestionType())
                .choiceList(getChoiceDetailDtos(questionDocument))
                .wordCloudDtos(getWordCloudDtos(questionDocument))
                .build();
    }

    @NotNull
    private List<WordCloudDto> getWordCloudDtos(QuestionDocument questionDocument) {
        return questionDocument.getWordCloudList().stream().map(this::entityToDto).toList();
    }

    @NotNull
    private List<ChoiceDetailDto> getChoiceDetailDtos(QuestionDocument questionDocument) {
        if (questionDocument.getQuestionType() == 0) {
            // 주관식 처리
            return apiService.getQuestionAnswersByCheckAnswerId(questionDocument.getId()).stream()
                    .filter(questionAnswer -> questionAnswer.getQuestionType() == 0)
                    .map(questionAnswer -> entityToDto(Choice.builder()
                            .questionDocument(questionDocument)
                            .title(questionAnswer.getCheckAnswer())
                            .count(0)
                            .build()))
                    .collect(Collectors.toList());
        } else {
            // 객관식 또는 찬반식 처리
            return questionDocument.getChoiceList().stream()
                    .map(this::entityToDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public ChoiceDetailDto entityToDto(Choice choice) {
        return ChoiceDetailDto.builder()
                .id(choice.getId())
                .title(choice.getTitle())
                .count(choice.getCount())
                .build();
    }

    @Override
    public WordCloudDto entityToDto(WordCloud wordCloud) {
        return WordCloudDto.builder()
                .id(wordCloud.getId())
                .title(wordCloud.getTitle())
                .count(wordCloud.getCount())
                .build();
    }

    @Override
    public DesignResponseDto entityToDto(Design design) {
        return DesignResponseDto.builder()
                .font(design.getFont())
                .fontSize(design.getFontSize())
                .backColor(design.getBackColor())
                .build();
    }

    @Override
    public ManagementResponseDto entityToDto(DateManagement dateManagement) {
        return ManagementResponseDto.builder()
                .startDate(dateManagement.getStartDate())
                .endDate(dateManagement.getDeadline())
                .enable(dateManagement.getIsEnabled())
                .build();
    }

    @Override
    public SurveyDetailDto entityToDto1(SurveyDocument surveyDocument) {
        return SurveyDetailDto.builder()
                .id(surveyDocument.getId())
                .title(surveyDocument.getTitle())
                .description(surveyDocument.getDescription())
                .reliability(surveyDocument.getReliability())
                .design(entityToDto(surveyDocument.getDesign()))
                .manage(entityToDto(surveyDocument.getDate()))
                .questionList(getQuestionDetailDtos(surveyDocument))
                .build();
    }

    @Override
    public SurveyDetailDto2 entityToDto2(SurveyDocument surveyDocument) {
        return SurveyDetailDto2.builder()
                .id(surveyDocument.getId())
                .title(surveyDocument.getTitle())
                .description(surveyDocument.getDescription())
                .questionList(getQuestionDetailDtos(surveyDocument))
                .build();
    }

    @Override
    public Choice DtoToEntity(ChoiceRequestDto choiceRequestDto, QuestionDocument questionDocument) {
        return Choice.builder()
                .questionDocument(questionDocument)
                .title(choiceRequestDto.getChoiceName())
                .count(0)
                .build();
    }

    @Override
    public QuestionDocument DtoToEntity(QuestionRequestDto questionRequestDto, SurveyDocument surveyDocument) {
        return QuestionDocument.builder()
                .surveyDocument(surveyDocument)
                .title(questionRequestDto.getTitle())
                .questionType(questionRequestDto.getType())
                .build();
    }

    @Override
    public DateManagement DtoToEntity(DateDto dateDto, SurveyDocument surveyDocument) {
        return DateManagement.builder()
                .surveyDocument(surveyDocument)
                .startDate(dateDto.getStartDate())
                .deadline(dateDto.getEndDate())
                .isEnabled(dateDto.getEnable())
                .build();
    }

    @Override
    public Design DtoToEntity(DesignRequestDto designRequestDto, SurveyDocument surveyDocument) {
        return Design.builder()
                .surveyDocument(surveyDocument)
                .font(designRequestDto.getFont())
                .backColor(designRequestDto.getBackColor())
                .fontSize(designRequestDto.getFontSize())
                .build();
    }

    @Override
    public SurveyDocument DtoToEntity(SurveyRequestDto surveyRequestDto, Long userId) {
        return SurveyDocument.builder()
                .userId(userId)
                .title(surveyRequestDto.getTitle())
                .description(surveyRequestDto.getDescription())
                .type(surveyRequestDto.getType())
                .reliability(surveyRequestDto.getReliability()) // 진정성 검사
                .countAnswer(0)
                .build();
    }

    @NotNull
    private List<QuestionDetailDto> getQuestionDetailDtos(SurveyDocument surveyDocument) {
        return surveyDocument.getQuestionDocumentList().stream().map(this::entityToDto).toList();
    }
}
