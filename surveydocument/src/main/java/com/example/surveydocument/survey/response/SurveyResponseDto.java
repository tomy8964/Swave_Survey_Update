package com.example.surveydocument.survey.response;

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

    //    @ConstructorProperties({"title", "description", "type", "questionResponse"})
    @Builder
    public SurveyResponseDto(String title, String description, int type,String font,int fontSize,String backColor, List<QuestionResponseDto> questionResponse) {        this.title = title;
        this.description = description;
        this.type = type;
        this.font=font;
        this.fontSize=fontSize;
        this.backColor=backColor;
        this.questionResponse = questionResponse;
    }
}
