package com.example.surveydocument.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;

public class QuestionTemplateAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "survey_template_id")
    private Long surveyTemplateId;

    @ManyToOne
    @JsonIgnore // 순환참조 방지
    @JoinColumn(name = "survey_answer_id")
    private SurveyAnswer surveyAnswerId;

    // 생성자 오버로딩
    @Builder
    public QuestionTemplateAnswer(Long surveyTemplateId, String title, SurveyAnswer surveyAnswerId, int questionType, String checkAnswer, Long checkAnswerId) {
        this.title = title;
        this.questionType = questionType;
        this.surveyAnswerId = surveyAnswerId;
        this.checkAnswer = checkAnswer;
        this.checkAnswerId = checkAnswerId;
        this.surveyTemplateId = surveyTemplateId;
    }
}
