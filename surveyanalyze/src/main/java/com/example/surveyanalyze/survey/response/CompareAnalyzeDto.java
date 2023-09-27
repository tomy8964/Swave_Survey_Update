package com.example.surveyanalyze.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CompareAnalyzeDto {
    private Long id;
    private String questionTitle;
    private Double pValue;

    @Builder
    public CompareAnalyzeDto(Long id, String questionTitle, Double pValue) {
        this.id = id;
        this.questionTitle = questionTitle;
        this.pValue = pValue;
    }
}
