package com.example.surveyanswer.survey.response;

import com.example.surveyanswer.survey.domain.Design;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyDetailDto implements Serializable {
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
    DesignResponseDto design;

}
