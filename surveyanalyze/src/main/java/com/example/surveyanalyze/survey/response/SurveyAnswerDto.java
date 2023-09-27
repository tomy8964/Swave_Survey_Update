package com.example.surveyanalyze.survey.response;

import lombok.Builder;
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

    @Builder
    public SurveyAnswerDto(Long id, int type, String title, String description, List<QuestionAnswerDto> questionAnswersList, Long surveyDocumentId) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.questionAnswersList = questionAnswersList;
        this.surveyDocumentId = surveyDocumentId;
    }
}
