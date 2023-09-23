package com.example.surveydocument.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class QuestionTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_template_id")
    private Long id;
    @Column(name = "question_title")
    private String title;
    @Column(name = "question_type")
    private int questionType;

    @OneToMany(mappedBy = "question_template_id", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Column(name = "choice_list")
    private List<ChoiceTemplate> choiceTemplateList;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "survey_template_id")
    private SurveyTemplate surveyTemplateId;

    // 생성자 오버로딩
    @Builder
    // 객관식 생성자
    public QuestionTemplate(SurveyTemplate surveyTemplate, String title, int questionType, List<ChoiceTemplate> choiceTemplateList) {
        this.surveyTemplateId = surveyTemplate;
        this.title = title;
        this.questionType = questionType;
        this.choiceTemplateList = choiceTemplateList;
    }

    @Builder
    // 주관식, 찬부신 생성자
    public QuestionTemplate(String title, int questionType) {
        this.title = title;
        this.questionType = questionType;
    }

    public void setChoice(ChoiceTemplate choice) {
        this.choiceTemplateList.add(choice);
    }
}
