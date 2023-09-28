package com.example.surveyanalyze.survey.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SurveyAnswerDto {
    private Long id;
    private int type;
    private String title;
    private String description;
    private List<QuestionAnswerDto> questionAnswersList;

    private Long surveyDocumentId;
}
