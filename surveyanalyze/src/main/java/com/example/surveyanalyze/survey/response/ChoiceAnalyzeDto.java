package com.example.surveyanalyze.survey.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceAnalyzeDto {
    private Long id;
    private double support;
    private String questionTitle;
    private String choiceTitle;
}
