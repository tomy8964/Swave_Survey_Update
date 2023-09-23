package com.example.surveyanswer.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionResponseDto {
    private String title;
    private int type;
    // 선택한 answer
    private String answer;
    // 선택한 answer의 id
    private Long answerId;


    // 주관식, 찬부식 + 객관식 -> 나중에 서비스에서 구분필요
    @Builder
    public QuestionResponseDto(String title, int type, String answer, Long answerId) {
        this.type = type;
        this.title = title;
        this.answer = answer;
        this.answerId = answerId;
    }
}
