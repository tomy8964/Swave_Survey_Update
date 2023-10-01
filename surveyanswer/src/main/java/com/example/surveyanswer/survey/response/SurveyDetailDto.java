package com.example.surveyanswer.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class SurveyDetailDto {
    private Long id;
    private String title;
    private String description;
    private int countAnswer;
    private List<QuestionDetailDto> questionList;
    Boolean reliability;

    private Date startDate;
    private Date endDate;
    private boolean enable;

    // 설문 참여 부분이기 때문에 디자인 필요
    private DesignResponseDto design;

    @Builder
    public SurveyDetailDto(Boolean reliability, Long id, String title, String description, int countAnswer, Date startDate, Date endDate, boolean enable, DesignResponseDto design, List<QuestionDetailDto> questionList) {
        this.reliability = reliability;
        this.id = id;
        this.title = title;
        this.description = description;
        this.countAnswer = countAnswer;
        this.startDate = startDate;
        this.endDate = endDate;
        this.enable = enable;
        this.design = design;
        this.questionList = questionList;
    }
}
