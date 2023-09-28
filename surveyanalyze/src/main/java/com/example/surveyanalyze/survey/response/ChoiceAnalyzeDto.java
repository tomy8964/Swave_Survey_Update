package com.example.surveyanalyze.survey.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChoiceAnalyzeDto {
    private Long id;
    private double support;
    private String questionTitle;
    private String choiceTitle;
}
