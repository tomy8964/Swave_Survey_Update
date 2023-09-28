package com.example.surveyanalyze.survey.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDetailDto {
    private Long id;
    private String title;
    private int questionType;

    @Builder.Default
    private List<ChoiceDetailDto> choiceList = new ArrayList<>();

    @Builder.Default
    private List<WordCloudDto> wordCloudDtos = new ArrayList<>();

}
