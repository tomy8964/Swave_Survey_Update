package com.example.surveyanalyze.survey.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequestDto {
    private String title;
    private int type;
    private List<ChoiceRequestDto> choiceList;

    /*
    HttpMessageConverter Error code : no Creators, like default constructor, exist
     */
    public QuestionRequestDto() {
    }

    ;

    // 객관식
    @Builder
    public QuestionRequestDto(String title, int type, List<ChoiceRequestDto> choiceList) {
        this.title = title;
        this.choiceList = choiceList;
        this.type = type;
    }

    @Builder
    public QuestionRequestDto(String title, int type) {
        this.title = title;
        this.type = type;
    }
}
