package com.example.surveydocument.survey.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
public class Design {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "design_id")
    private Long id;
    private String font;
    private int fontSize;
    private String backColor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_document_id")
    private SurveyDocument surveyDocument;

    @Builder
    public Design(Long id, String font, int fontSize, String backColor, SurveyDocument surveyDocument) {
        this.id = id;
        this.font = font;
        this.fontSize = fontSize;
        this.backColor = backColor;
        if (surveyDocument != null) {
            this.surveyDocument = surveyDocument;
            surveyDocument.setDesign(this);
        }
    }
}