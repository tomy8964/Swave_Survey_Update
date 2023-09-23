package com.example.surveyanalyze.survey.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAnalyzeDto {
    private Long id;
    private String questionTitle;
    private List<ChiAnalyzeDto> chiAnalyzeList;
    private List<CompareAnalyzeDto> compareAnalyzeList;
}
