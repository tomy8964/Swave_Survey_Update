package com.example.surveyanalyze.survey.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompareAnalyze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compare_analyze_id")
    private Long id;
    private String questionTitle;
    private Double pValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_analyze_id")
    private QuestionAnalyze questionAnalyze;

    @Builder
    public CompareAnalyze(String questionTitle, Double pValue, QuestionAnalyze questionAnalyze) {
        this.questionTitle = questionTitle;
        this.pValue = pValue;
        if (questionAnalyze != null) {
            this.questionAnalyze = questionAnalyze;
            questionAnalyze.getCompareAnalyzeList().add(this);
        }
    }
}
