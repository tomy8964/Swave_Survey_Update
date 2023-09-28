package com.example.surveydocument.survey.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequestDto {
    private String title;
    private int type;
    private List<ChoiceRequestDto> choiceList;
}
