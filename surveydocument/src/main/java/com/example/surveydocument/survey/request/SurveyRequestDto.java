package com.example.surveydocument.survey.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyRequestDto {
    private String title;
    private String description;
    private int type;
    private Boolean reliability;
    private DesignRequestDto design;
    private Date startDate;
    private Date endDate;
    private Boolean enable;
    private List<QuestionRequestDto> questionRequest;
}
