package com.example.surveyanalyze.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SurveyDetailDto {
    private Long id;
    private String title;
    private String description;
    private int countAnswer;
    private List<QuestionDetailDto> questionList;

    @Builder
    public SurveyDetailDto(Long id, String title, String description, int countAnswer, List<QuestionDetailDto> questionList) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.countAnswer = countAnswer;
        this.questionList = questionList;
    }
}
