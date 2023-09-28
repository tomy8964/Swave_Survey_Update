package com.example.surveydocument.survey.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@Entity
@NoArgsConstructor
public class DateManagement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "date_id")
    private long id;

    @Column(name = "survey_start_date")
    private Date startDate;

    @Column(name = "survey_deadline")
    private Date deadline;
    @Column(name = "survey_enable")
    private Boolean isEnabled;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_document_id")
    private SurveyDocument surveyDocument;

    @Builder
    public DateManagement(Date startDate, Date deadline, Boolean isEnabled, SurveyDocument surveyDocument) {
        this.startDate = startDate;
        this.deadline = deadline;
        this.isEnabled = isEnabled;
        if (surveyDocument != null) {
            this.surveyDocument = surveyDocument;
            surveyDocument.setDate(this);
        }
    }

    public void setIsEnabled(Boolean enable) {
        this.isEnabled = enable;
    }
}
