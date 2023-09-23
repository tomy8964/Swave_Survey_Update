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
public class SurveyDetailDto {
    private Long id;
    private String title;
    private String description;
    //
    private int countAnswer;
    private List<QuestionDetailDto> questionList;

    // 파이차트 같은 응답 수치화 이기 때문에 design 필요 없음

    // getter, setter 생략
}
