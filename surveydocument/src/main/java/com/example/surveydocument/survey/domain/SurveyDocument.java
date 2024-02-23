package com.example.surveydocument.survey.domain;

import com.example.surveydocument.survey.request.ChoiceRequestDto;
import com.example.surveydocument.survey.request.QuestionRequestDto;
import com.example.surveydocument.survey.request.SurveyRequestDto;
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

    @OneToOne(mappedBy = "surveyDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private Design design;

    @OneToOne(mappedBy = "surveyDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private DateManagement date;

    private Boolean reliability;

    @Column(name = "content")
    @Builder.Default
    @OneToMany(mappedBy = "surveyDocument", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionDocument> questionDocumentList = new ArrayList<>();

    // 디자인 넣기
    public void setDesign(Design design) {
        this.design = design;
    }

    // 날짜 넣기
    public void setDate(DateManagement date) {
        this.date = date;
    }

    public void addCountAnswer() {
        this.countAnswer++;
    }

    public Long updateSurvey(SurveyRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.description = requestDto.getDescription();
        this.type = requestDto.getType();

        // Question List 수정
        // survey document 의 Question List 초기화
        this.getQuestionDocumentList().clear();

        for (QuestionRequestDto questionRequestDto : requestDto.getQuestionRequest()) {
            QuestionDocument question = QuestionDocument.builder()
                    .surveyDocument(this)
                    .title(questionRequestDto.getTitle())
                    .questionType(questionRequestDto.getType())
                    .build();

            if (questionRequestDto.getType() == 0) continue; // 주관식

            // 객관식, 찬부식일 경우 선지 저장
            for (ChoiceRequestDto choiceRequestDto : questionRequestDto.getChoiceList()) {
                Choice.builder()
                        .questionDocument(question)
                        .title(choiceRequestDto.getChoiceName())
                        .count(0)
                        .build();
            }
        }
        return this.id;
    }
}
