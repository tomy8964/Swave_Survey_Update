package com.example.surveydocument.survey.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDto {
    private String title;
    private int type;
    private List<ChoiceRequestDto> choiceList;
}
