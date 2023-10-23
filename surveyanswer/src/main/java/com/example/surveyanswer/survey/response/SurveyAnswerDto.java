package com.example.surveyanswer.survey.response;

import com.example.surveyanswer.survey.domain.QuestionAnswer;
import com.example.surveyanswer.survey.domain.SurveyAnswer;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class SurveyAnswerDto {
    private Long id;
    private int type;
    private String title;
    private String description;
    private List<QuestionAnswerDto2> questionAnswersListDto2 = new ArrayList<>();

    @Column(name = "survey_document_Id")
    private Long surveyDocumentId;

    @Builder
    public SurveyAnswerDto(SurveyAnswer surveyAnswer) {
        this.id = surveyAnswer.getId();
        this.type = surveyAnswer.getType();
        this.title = surveyAnswer.getTitle();
        this.description = surveyAnswer.getDescription();
        List<QuestionAnswer> questionAnswersList = surveyAnswer.getQuestionAnswersList();
        this.questionAnswersListDto2 = questionAnswersList.stream().map(QuestionAnswerDto2::new).collect(Collectors.toList());
        this.surveyDocumentId = surveyAnswer.getSurveyDocumentId();
    }
}
