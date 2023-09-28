package com.example.surveyanalyze.survey.request;

import lombok.Data;

import java.util.List;

@Data
public class QuestionRequestDto {
    private String title;
    private int type;
    private List<ChoiceRequestDto> choiceList;
}
