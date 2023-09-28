package com.example.surveyanalyze.survey.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SurveyAnalyzeDto {
    private Long id;
    private List<QuestionAnalyzeDto> questionAnalyzeList;
    private List<AprioriAnalyzeDto> aprioriAnalyzeList;
}
