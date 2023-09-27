package com.example.surveyanalyze.survey.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SurveyAnalyze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_analyze_id")
    private Long id;
    private Long surveyDocumentId;

    @Builder.Default
    @OneToMany(mappedBy = "surveyAnalyze", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<QuestionAnalyze> questionAnalyzeList = new ArrayList<>();

    @Builder.Default
    @Column(name = "apriori_list")
    @OneToMany(mappedBy = "surveyAnalyze", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AprioriAnalyze> aprioriAnalyzeList = new ArrayList<>();
}
