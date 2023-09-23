package com.example.surveydocument.survey.request;

import com.example.surveydocument.survey.domain.Design;
import com.example.surveydocument.survey.domain.DesignTemplate;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SurveyTemplateRequestDTO {
    String title;
    String description;
    int type;
    List<QuestionRequestDto> questionRequest;
    Boolean reliability;
    DesignTemplate design;
    String startDate;
    String endDate;

    // todo : enable

    @Builder
    public SurveyTemplateRequestDTO(
            String startDate, String endDate, String title, String description, int type, List<QuestionRequestDto> questionRequest, DesignTemplate design, Boolean reliability) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.reliability=reliability;
        this.design = design;
        this.questionRequest = questionRequest;
    }
}
