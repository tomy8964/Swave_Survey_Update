package com.example.surveyanalyze.survey.response;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionAnswerDto {
    private Long id;
    private String title;
    private int questionType;
    private String checkAnswer;
    private Long checkAnswerId;
    private Long surveyDocumentId;

    @Builder
    public QuestionAnswerDto(Long id, String title, int questionType, String checkAnswer, Long checkAnswerId, Long surveyDocumentId) {
        this.id = id;
        this.title = title;
        this.questionType = questionType;
        this.checkAnswer = checkAnswer;
        this.checkAnswerId = checkAnswerId;
        this.surveyDocumentId = surveyDocumentId;
    }
}
