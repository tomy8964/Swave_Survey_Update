package com.example.surveyanswer.survey.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class SurveyDetailDto {
    Boolean reliability;
    private Long id;
    private String title;
    private String description;
    private int countAnswer;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startDate;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;
    private boolean enable;
    // 설문 참여 부분이기 때문에 디자인 필요
    private DesignResponseDto design;
    private List<QuestionDetailDto> questionList;

    @Builder
    public SurveyDetailDto(Boolean reliability, Long id, String title, String description, int countAnswer, LocalDateTime startDate, LocalDateTime endDate, boolean enable, DesignResponseDto design, List<QuestionDetailDto> questionList) {
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
