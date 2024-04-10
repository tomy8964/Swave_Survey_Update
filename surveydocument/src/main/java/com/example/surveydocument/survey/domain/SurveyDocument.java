package com.example.surveydocument.survey.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity(name = "survey_document")
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE survey_document SET is_deleted = true WHERE survey_document_id = ?")
public class SurveyDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_document_id")
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "survey_title")
    private String title;
    @Column(name = "survey_type")
    private int type;
    @Column(name = "survey_description")
    private String description;
    @Column(name = "accept_response")
    private boolean acceptResponse;

    @Builder.Default
    @Column(name = "answer_count")
    private int countAnswer = 0;

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = Boolean.FALSE;

    @Setter
    @OneToOne(mappedBy = "surveyDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private Design design;

    @Setter
    @OneToOne(mappedBy = "surveyDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private DateManagement date;

    private Boolean reliability;

    @Column(name = "content")
    @Builder.Default
    @OneToMany(mappedBy = "surveyDocument", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionDocument> questionDocumentList = new ArrayList<>();

    public void addCountAnswer() {
        this.countAnswer++;
    }
}
