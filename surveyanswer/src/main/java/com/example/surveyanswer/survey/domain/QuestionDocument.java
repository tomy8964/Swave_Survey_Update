package com.example.surveyanswer.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class QuestionDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;
    @Column(name = "question_title")
    private String title;
    @Column(name = "question_type")
    private int questionType;

    @OneToMany(mappedBy = "questionDocument", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Column(name = "wordCloud_list")
    private List<WordCloud> wordCloudList;

    @OneToMany(mappedBy = "question_id", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Column(name = "choice_list")
    private List<Choice> choiceList;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "survey_document_id")
    private SurveyDocument surveyDocumentId;

    // 생성자 오버로딩
    @Builder
    // 객관식 생성자
    public QuestionDocument(List<WordCloud> wordCloudList, SurveyDocument surveyDocument, String title, int questionType, List<Choice> choiceList) {
        this.surveyDocumentId = surveyDocument;
        this.title = title;
        this.questionType = questionType;
        this.choiceList = choiceList;
        this.wordCloudList = wordCloudList;
    }

    @Builder
    // 주관식, 찬부신 생성자
    public QuestionDocument(String title, int questionType) {
        this.title = title;
        this.questionType = questionType;
    }

    public void setChoice(Choice choice) {
        this.choiceList.add(choice);
    }
}
