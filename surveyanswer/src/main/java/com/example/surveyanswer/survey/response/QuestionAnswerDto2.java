package com.example.surveyanswer.survey.response;

import com.example.surveyanswer.survey.domain.QuestionAnswer;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class QuestionAnswerDto2 {
    private Long id;
    private String title;
    private int questionType;
    private String checkAnswer;
    private Long checkAnswerId;
    private Long surveyDocumentId;

    @Builder
    @QueryProjection
    public QuestionAnswerDto2(QuestionAnswer questionAnswer) {
        this.id = questionAnswer.getId();
        this.title = questionAnswer.getTitle();
        this.questionType = questionAnswer.getQuestionType();
        this.checkAnswer = questionAnswer.getCheckAnswer();
        this.checkAnswerId = questionAnswer.getCheckAnswerId();
        this.surveyDocumentId = questionAnswer.getSurveyDocumentId();
    }
}
