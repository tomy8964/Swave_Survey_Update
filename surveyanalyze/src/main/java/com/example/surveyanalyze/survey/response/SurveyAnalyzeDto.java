package com.example.surveyanalyze.survey.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyAnalyzeDto {
    private Long id;
    private List<QuestionAnalyzeDto> questionAnalyzeList;
    private List<AprioriAnalyzeDto> aprioriAnalyzeList;

}
