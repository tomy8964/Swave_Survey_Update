package com.example.surveyanalyze.survey.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChiAnalyzeDto {
    private Long id;
    private String questionTitle;
    private Double pValue;
}
