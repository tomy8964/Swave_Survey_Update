package com.example.surveydocument.survey.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyDetailDto {
    private Long id;
    private String title;
    private String description;
    private int countAnswer;
    private List<QuestionDetailDto> questionList;
    private Boolean reliability;

    private Date startDate;
    private Date endDate;
    private Boolean enable;

    DesignResponseDto design;
    // getter, setter 생략
}
