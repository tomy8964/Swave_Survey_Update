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
public class QuestionAnalyze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_analyze_id")
    private Long id;
    private String questionTitle;
    private String wordCloud;

    @Builder.Default
    @OneToMany(mappedBy = "questionAnalyze", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ChiAnalyze> chiAnalyzeList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "questionAnalyze", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CompareAnalyze> compareAnalyzeList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_analyze_id")
    private SurveyAnalyze surveyAnalyze;
}
