package com.example.surveyanalyze.survey.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AprioriAnalyzeDto {
    private Long id;
    private String questionTitle;
    private String choiceTitle;
    private List<ChoiceAnalyzeDto> choiceAnalyzeList;
}
