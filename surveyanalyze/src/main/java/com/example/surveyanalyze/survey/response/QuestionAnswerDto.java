package com.example.surveyanalyze.survey.response;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionAnswerDto {
    private Long id;
    private int questionType;
    private String checkAnswer;

    @Builder
    public QuestionAnswerDto(Long id, int questionType, String checkAnswer) {
        this.id = id;
        this.questionType = questionType;
        this.checkAnswer = checkAnswer;
    }
}
