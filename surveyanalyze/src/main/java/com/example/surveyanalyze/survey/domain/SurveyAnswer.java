package com.example.surveyanalyze.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class SurveyAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_answer_id")
    private Long id;
    @Column(name = "survey_type")
    private int type;
    @Column(name = "survey_title")
    private String title;
    @Column(name = "survey_description")
    private String description;

    @OneToMany(mappedBy = "surveyAnswerId", fetch = FetchType.LAZY)
    @JsonIgnore //순환참조 방지
    @Column(name = "content")
    private List<QuestionAnswer> questionanswersList;

    @Column(name = "survey_document_Id")
    private Long surveyDocumentId;

    @Builder
    public SurveyAnswer(Long surveyDocumentId, String title, int type, String description, List<QuestionAnswer> questionAnswerList) {
        this.surveyDocumentId = surveyDocumentId;
        this.title = title;
        this.type = type;
        this.description = description;
        this.questionanswersList = questionAnswerList;
    }

    // 문항 List 넣어주기
    public void setQuestion(QuestionAnswer questionanswer) {
        this.questionanswersList.add(questionanswer);
    }

}
