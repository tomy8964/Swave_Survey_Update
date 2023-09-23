package com.example.surveydocument.survey.request;

import com.example.surveydocument.survey.domain.Design;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class SurveyRequestDto {
    String title;
    String description;
    int type;
    List<QuestionRequestDto> questionRequest;
    Boolean reliability;
    DesignRequestDto design;
    Date startDate;
    Date endDate;

    // 설문 공개 여부
    Boolean enable;

    @Builder
    public SurveyRequestDto(Boolean enable,
            Date startDate, Date endDate, String title, String description, int type, List<QuestionRequestDto> questionRequest, DesignRequestDto design, Boolean reliability) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.reliability=reliability;
        this.design = design;
        this.questionRequest = questionRequest;
        this.enable=enable;
    }
}
