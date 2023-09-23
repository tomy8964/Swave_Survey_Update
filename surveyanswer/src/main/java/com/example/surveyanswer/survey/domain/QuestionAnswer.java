package com.example.surveyanswer.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class QuestionAnswer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_answer_id")
    private Long id;
    @Column(name = "question_title")
    private String title;
    @Column(name = "question_type")
    private int questionType;
    @Column(name = "check_answer")
    private String checkAnswer;
    @Column(name = "check_answer_id")
    private Long checkAnswerId;

    @Column(name = "survey_document_id")
    private Long surveyDocumentId;

    @ManyToOne
    @JsonIgnore // 순환참조 방지
    @JoinColumn(name = "survey_answer_id")
    private SurveyAnswer surveyAnswerId;

    @Builder
    public QuestionAnswer(Long surveyDocumentId, String title, SurveyAnswer surveyAnswerId, int questionType, String checkAnswer, Long checkAnswerId) {
        this.title = title;
        this.questionType = questionType;
        this.surveyAnswerId = surveyAnswerId;
        this.checkAnswer = checkAnswer;
        this.checkAnswerId = checkAnswerId;
        this.surveyDocumentId = surveyDocumentId;
    }
}
