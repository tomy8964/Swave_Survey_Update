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
public class AprioriAnalyze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apriori_analyze_id")
    private Long id;
    private Long choiceId;
    private String questionTitle;
    private String choiceTitle;

    @Builder.Default
    @Column(name = "choice_list")
    @OneToMany(mappedBy = "aprioriAnalyze", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ChoiceAnalyze> choiceAnalyzeList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_analyze_id")
    private SurveyAnalyze surveyAnalyze;
}
