package com.example.surveyanalyze.survey.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @OneToMany(mappedBy = "aprioriAnalyze", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChoiceAnalyze> choiceAnalyzeList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_analyze_id")
    private SurveyAnalyze surveyAnalyze;

    public AprioriAnalyze(Long id, Long choiceId, String questionTitle, String choiceTitle, List<ChoiceAnalyze> choiceAnalyzeList, SurveyAnalyze surveyAnalyze) {
        this.id = id;
        this.choiceId = choiceId;
        this.questionTitle = questionTitle;
        this.choiceTitle = choiceTitle;
        this.choiceAnalyzeList = choiceAnalyzeList;
        if (surveyAnalyze != null) {
            this.surveyAnalyze = surveyAnalyze;
            surveyAnalyze.getAprioriAnalyzeList().add(this);
        }
    }
}
