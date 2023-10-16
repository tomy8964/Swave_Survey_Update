package com.example.surveydocument.survey.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SurveyDetailDto2 {
    private Long id;
    private String title;
    private String description;
    private int countAnswer;
    private List<QuestionDetailDto> questionList;
}
