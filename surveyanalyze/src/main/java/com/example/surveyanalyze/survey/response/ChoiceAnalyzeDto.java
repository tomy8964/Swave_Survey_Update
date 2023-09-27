package com.example.surveyanalyze.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChoiceAnalyzeDto {
    private Long id;
    private double support;
    private String questionTitle;
    private String choiceTitle;

    @Builder
    public ChoiceAnalyzeDto(Long id, double support, String questionTitle, String choiceTitle) {
        this.id = id;
        this.support = support;
        this.questionTitle = questionTitle;
        this.choiceTitle = choiceTitle;
    }
}
