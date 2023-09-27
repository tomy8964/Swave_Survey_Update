package com.example.surveyanalyze.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class QuestionDetailDto {
    private Long id;
    private String title;
    private int questionType;
    private List<ChoiceDetailDto> choiceList;
    private List<WordCloudDto> wordCloudDtos;

    @Builder
    public QuestionDetailDto(Long id, String title, int questionType, List<ChoiceDetailDto> choiceList, List<WordCloudDto> wordCloudDtos) {
        this.id = id;
        this.title = title;
        this.questionType = questionType;
        this.choiceList = choiceList;
        this.wordCloudDtos = wordCloudDtos;
    }
}
