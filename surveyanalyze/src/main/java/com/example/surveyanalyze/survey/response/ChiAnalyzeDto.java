package com.example.surveyanalyze.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChiAnalyzeDto {
    private Long id;
    private String questionTitle;
    private Double pValue;

    @Builder
    public ChiAnalyzeDto(Long id, String questionTitle, Double pValue) {
        this.id = id;
        this.questionTitle = questionTitle;
        this.pValue = pValue;
    }
}
