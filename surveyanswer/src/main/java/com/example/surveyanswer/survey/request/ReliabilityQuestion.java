package com.example.surveyanswer.survey.request;

import com.example.surveyanswer.survey.response.ChoiceDetailDto;
import com.example.surveyanswer.survey.response.QuestionDetailDto;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ReliabilityQuestion {
    public static final long RELIABILITY_QUESTION_ID = -1L;
    private String title;
    private int type;
    private List<ReliabilityChoice> choiceList = new ArrayList<>();

    private String correctAnswer;

    @Builder
    public ReliabilityQuestion(String title, int type, List<ReliabilityChoice> choiceList, String correctAnswer) {
        this.title = title;
        this.type = type;
        this.choiceList = choiceList;
        this.correctAnswer = correctAnswer;
    }

    public QuestionDetailDto toQuestionDetailDto() {
        QuestionDetailDto questionDetailDto = QuestionDetailDto.builder()
                .id(RELIABILITY_QUESTION_ID)
                .questionType(this.getType())
                .title(this.getTitle())
                .choiceList(new ArrayList<>())
                .build();

        for (ReliabilityChoice reliabilityChoice : this.getChoiceList()) {
            ChoiceDetailDto choiceDetailDto = ChoiceDetailDto.builder()
                    .id(RELIABILITY_QUESTION_ID)
                    .title(reliabilityChoice.getChoiceName())
                    .count(0)
                    .build();
            questionDetailDto.getChoiceList().add(choiceDetailDto);
        }
        return questionDetailDto;
    }
}
