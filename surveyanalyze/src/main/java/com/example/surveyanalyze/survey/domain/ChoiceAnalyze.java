package com.example.surveyanalyze.survey.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChoiceAnalyze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "choice_analyze_id")
    private Long id;
    private double support;
    private long choiceId;
    private String questionTitle;
    private String choiceTitle;

    @ManyToOne
    @JoinColumn(name = "apriori_analyze_id")
    private AprioriAnalyze aprioriAnalyze;

    @Builder
    public ChoiceAnalyze(Long id, double support, long choiceId, String questionTitle, String choiceTitle, AprioriAnalyze aprioriAnalyze) {
        this.id = id;
        this.support = support;
        this.choiceId = choiceId;
        this.questionTitle = questionTitle;
        this.choiceTitle = choiceTitle;
        if (aprioriAnalyze != null) {
            this.aprioriAnalyze = aprioriAnalyze;
            aprioriAnalyze.getChoiceAnalyzeList().add(this);
        }
    }
}
