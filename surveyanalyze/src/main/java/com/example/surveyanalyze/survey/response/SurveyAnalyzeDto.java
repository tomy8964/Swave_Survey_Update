package com.example.surveyanalyze.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SurveyAnalyzeDto {
    private Long id;
    private List<QuestionAnalyzeDto> questionAnalyzeList;
    private List<AprioriAnalyzeDto> aprioriAnalyzeList;

    @Builder
    public SurveyAnalyzeDto(Long id, List<QuestionAnalyzeDto> questionAnalyzeList, List<AprioriAnalyzeDto> aprioriAnalyzeList) {
        this.id = id;
        this.questionAnalyzeList = questionAnalyzeList;
        this.aprioriAnalyzeList = aprioriAnalyzeList;
    }
}
