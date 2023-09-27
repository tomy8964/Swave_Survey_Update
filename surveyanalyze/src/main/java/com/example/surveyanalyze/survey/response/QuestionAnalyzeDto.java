package com.example.surveyanalyze.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class QuestionAnalyzeDto {
    private Long id;
    private String questionTitle;
    private List<ChiAnalyzeDto> chiAnalyzeList;
    private List<CompareAnalyzeDto> compareAnalyzeList;

    @Builder
    public QuestionAnalyzeDto(Long id, String questionTitle, List<ChiAnalyzeDto> chiAnalyzeList, List<CompareAnalyzeDto> compareAnalyzeList) {
        this.id = id;
        this.questionTitle = questionTitle;
        this.chiAnalyzeList = chiAnalyzeList;
        this.compareAnalyzeList = compareAnalyzeList;
    }
}
