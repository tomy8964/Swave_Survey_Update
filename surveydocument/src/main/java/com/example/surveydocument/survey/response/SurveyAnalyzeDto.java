package com.example.surveydocument.survey.response;

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
}
