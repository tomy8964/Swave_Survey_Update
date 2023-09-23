package com.example.surveyanalyze.survey.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class SurveyAnalyze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_analyze_id")
    private Long id;

    @Column(name = "survey_document_id")
    private Long surveyDocumentId;

    @OneToMany(mappedBy = "surveyAnalyzeId", fetch = FetchType.LAZY)
    @Column(name = "question_analyze_list")
    private List<QuestionAnalyze> questionAnalyzeList;


    @OneToMany(mappedBy = "surveyAnalyzeId", fetch = FetchType.LAZY)
    @Column(name = "apriori_list")
    private List<AprioriAnalyze> aprioriAnalyzeList;


    @Builder
    public SurveyAnalyze(List<AprioriAnalyze> aprioriAnalyzeList, List<QuestionAnalyze> questionAnalyzeList, Long surveyDocumentId) {
        this.questionAnalyzeList = questionAnalyzeList;
        this.surveyDocumentId = surveyDocumentId;
        this.aprioriAnalyzeList = aprioriAnalyzeList;
    }
}
