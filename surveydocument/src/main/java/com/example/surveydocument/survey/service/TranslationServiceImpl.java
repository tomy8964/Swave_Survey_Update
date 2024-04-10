package com.example.surveydocument.survey.service;

import com.example.surveydocument.restAPI.service.RestApiService;
import com.example.surveydocument.survey.domain.*;
import com.example.surveydocument.survey.request.*;
import com.example.surveydocument.survey.response.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {
    private final RestApiService apiService;

    @Override
    public QuestionDetailDto entityToDto(QuestionDocument questionDocument) {
        QuestionDetailDto questionDto = new QuestionDetailDto();
        questionDto.setId(questionDocument.getId());
        questionDto.setTitle(questionDocument.getTitle());
        questionDto.setQuestionType(questionDocument.getQuestionType());

        questionDto.setChoiceList(getChoiceDetailDtos(questionDocument));
        questionDto.setWordCloudDtos(getWordCloudDtos(questionDocument));

        return questionDto;
    }

    @NotNull
    private List<WordCloudDto> getWordCloudDtos(QuestionDocument questionDocument) {
        List<WordCloudDto> wordCloudDtos = new ArrayList<>();
        for (WordCloud wordCloud : questionDocument.getWordCloudList()) {
            wordCloudDtos.add(entityToDto(wordCloud));
        }
        return wordCloudDtos;
    }

    @NotNull
    private List<ChoiceDetailDto> getChoiceDetailDtos(QuestionDocument questionDocument) {
        // question type에 따라 choice 에 들어갈 내용 구분
        // 주관식이면 choice title에 주관식 응답을 저장
        // 객관식 찬부식 -> 기존 방식 과 똑같이 count를 올려서 저장
        List<ChoiceDetailDto> choiceDtos = new ArrayList<>();
        if (questionDocument.getQuestionType() == 0) {
            // 주관식 답변들 리스트
            // REST API GET questionAnswersByCheckAnswerId
            List<QuestionAnswerDto> questionAnswerList = apiService.getQuestionAnswersByCheckAnswerId(questionDocument.getId());
            for (QuestionAnswerDto questionAnswer : questionAnswerList) {
                // 그 중에 주관식 답변만
                if (questionAnswer.getQuestionType() == 0) {
                    choiceDtos.add(entityToDto(Choice.builder()
                            .questionDocument(questionDocument)
                            .title(questionAnswer.getCheckAnswer())
                            .count(0)
                            .build()));
                }
            }
        } else {
            for (Choice choice : questionDocument.getChoiceList()) {
                choiceDtos.add(entityToDto(choice));
            }
        }
        return choiceDtos;
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
        List<QuestionDetailDto> questionDtos = new ArrayList<>();
        for (QuestionDocument questionDocument : surveyDocument.getQuestionDocumentList()) {
            questionDtos.add(entityToDto(questionDocument));
        }
        return questionDtos;
    }
}
