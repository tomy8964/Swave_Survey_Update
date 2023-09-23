package com.example.surveyanswer.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SurveyResponseDto {
    Long id;
    String title;
    String description;
    int type;
    List<QuestionResponseDto> questionResponse;
    String font;
    int fontSize;
    String backColor;
    Boolean reliability;

    @Builder
    public SurveyResponseDto(Long id, String title, String description, int type,String font,int fontSize,String backColor,Boolean reliability, List<QuestionResponseDto> questionResponse) {        this.title = title;
        this.id = id;
        this.description = description;
        this.type = type;
        this.font=font;
        this.fontSize=fontSize;
        this.backColor=backColor;
        this.reliability=reliability;
        this.questionResponse = questionResponse;
    }
}
