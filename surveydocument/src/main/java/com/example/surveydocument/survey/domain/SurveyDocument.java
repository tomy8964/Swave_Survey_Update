package com.example.surveydocument.survey.domain;

import com.example.surveydocument.survey.request.DateDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class SurveyDocument {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_document_id")
    private Long id;
    @Column(name = "survey_title")
    private String title;
    @Column(name = "survey_type")
    private int type;
    @Column(name = "survey_description")
    private String description;
    @Column(name = "accept_response")
    private boolean acceptResponse;

    @Column(name = "answer_count")
    private int countAnswer;

    @Column(name = "isDeleted")
    private boolean isDeleted = false;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JsonIgnore // 순환참조 방지
    @JoinColumn(name = "Design_id")
    private Design design;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JsonIgnore // 순환참조 방지
    @JoinColumn(name = "Date_id")
    private DateManagement date;

    @Column(name = "reliability")
    private Boolean reliability;

    @Column(name = "content")
    @OneToMany(mappedBy = "surveyDocumentId", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<QuestionDocument> questionDocumentList;

    @ManyToOne
    @JsonIgnore // 순환참조 방지
    @JoinColumn(name = "survey_id")
    private Survey survey;

    @Builder
    public SurveyDocument(int countAnswer, Survey survey, String title, int type,Boolean reliability, String description, List<QuestionDocument> questionDocumentList, DateManagement dateManagement, Design design) {
        this.survey = survey;
        this.title = title;
        this.type = type;
        this.description = description;
        this.questionDocumentList = questionDocumentList;
        this.reliability=reliability;
        this.countAnswer = countAnswer;
        this.date = dateManagement;
        this.design = design;
    }

    // 문항 list 에 넣어주기
    public void setQuestion(QuestionDocument questionDocument) {
        this.questionDocumentList.add(questionDocument);
    }

    // 날짜 넣기
    public void setDesign(Design design) {
        this.design = design;
    }

    // 디자인 넣기
    public void setDate(DateManagement date) {
        this.date = date;
    }
}
