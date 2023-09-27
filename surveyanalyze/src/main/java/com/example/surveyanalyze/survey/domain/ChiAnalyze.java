package com.example.surveyanalyze.survey.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChiAnalyze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chi_analyze_id")
    private Long id;
    private String questionTitle;
    private Double pValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_analyze_id")
    private QuestionAnalyze questionAnalyze;

    @Builder
    public ChiAnalyze(Long id, String questionTitle, Double pValue, QuestionAnalyze questionAnalyze) {
        this.id = id;
        this.questionTitle = questionTitle;
        this.pValue = pValue;
        if (questionAnalyze != null) {
            this.questionAnalyze = questionAnalyze;
            questionAnalyze.getChiAnalyzeList().add(this);
        }
    }
}
