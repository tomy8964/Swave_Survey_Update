package com.example.surveyanswer.survey.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(name = "content")
    @OneToMany(mappedBy = "surveyAnswer", fetch = FetchType.LAZY)
    private List<QuestionAnswer> questionAnswersList = new ArrayList<>();

    @Column(name = "survey_document_Id")
    private Long surveyDocumentId;

    @Builder
    public SurveyAnswer(Long surveyDocumentId, String title, int type, String description) {
        this.surveyDocumentId = surveyDocumentId;
        this.title = title;
        this.type = type;
        this.description = description;
    }
}
