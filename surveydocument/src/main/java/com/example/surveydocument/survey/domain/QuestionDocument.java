package com.example.surveydocument.survey.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE question_document SET is_deleted = true WHERE question_document_id = ?")
public class QuestionDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;
    @Column(name = "question_title")
    private String title;
    @Column(name = "question_type")
    private int questionType;

    @OneToMany(mappedBy = "questionDocument", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(name = "wordCloud_list")
    @Builder.Default
    private List<WordCloud> wordCloudList = new ArrayList<>();

    @OneToMany(mappedBy = "questionDocument", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(name = "choice_list")
    @Builder.Default
    private List<Choice> choiceList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_document_id")
    private SurveyDocument surveyDocument;

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = Boolean.FALSE;

    @Builder
    public QuestionDocument(String title, int questionType, List<WordCloud> wordCloudList, List<Choice> choiceList, SurveyDocument surveyDocument) {
        this.title = title;
        this.questionType = questionType;
        this.wordCloudList = wordCloudList;
        this.choiceList = choiceList;
        if (surveyDocument != null) {
            this.surveyDocument = surveyDocument;
            surveyDocument.getQuestionDocumentList().add(this);
        }
    }
}
