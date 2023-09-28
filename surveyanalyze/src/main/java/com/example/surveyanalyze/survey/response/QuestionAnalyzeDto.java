package com.example.surveyanalyze.survey.response;

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
}
