package com.example.surveyanalyze.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AprioriAnalyzeDto {
    private Long id;
    private String questionTitle;
    private String choiceTitle;
    private List<ChoiceAnalyzeDto> choiceAnalyzeList;

    @Builder
    public AprioriAnalyzeDto(Long id, String questionTitle, String choiceTitle, List<ChoiceAnalyzeDto> choiceAnalyzeList) {
        this.id = id;
        this.questionTitle = questionTitle;
        this.choiceTitle = choiceTitle;
        this.choiceAnalyzeList = choiceAnalyzeList;
    }
}
