package com.example.surveyanalyze.survey.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDetailDto {
    private Long id;
    private String title;
    private int questionType;
    private List<ChoiceDetailDto> choiceList;

    private List<WordCloudDto> wordCloudDtos;
    // getter, setter 생략
}
