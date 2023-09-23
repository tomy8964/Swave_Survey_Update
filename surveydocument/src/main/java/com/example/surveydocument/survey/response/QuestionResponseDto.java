package com.example.surveydocument.survey.response;

import lombok.Data;

@Data
public class QuestionResponseDto {
    private String title;
    private int type;
    // 선택한 answer
    private String answer;
    // 선택한 answer의 id
    private Long answerId;

    /*
    HttpMessageConverter Error code : no Creators, like default constructor, exist
     */
    public QuestionResponseDto() {};

    // 주관식, 찬부식 + 객관식 -> 나중에 서비스에서 구분필요
    public QuestionResponseDto(String title, int type, String answer, Long answerId) {
        this.type = type;
        this.title = title;
        this.answer = answer;
        this.answerId = answerId;
    }
}
