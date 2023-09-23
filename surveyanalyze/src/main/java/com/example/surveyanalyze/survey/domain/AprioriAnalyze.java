package com.example.surveyanalyze.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class AprioriAnalyze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apriori_analyze_id")
    private Long id;
    @Column(name = "choiceId(연관분석할 choiceId)")
    private Long choiceId;
    @Column(name = "question_title")
    private String questionTitle;
    @Column(name = "choice_title")
    private String choiceTitle;

    @OneToMany(mappedBy = "aprioriAnalyzeId", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore //순환참조 방지
    @Column(name = "choice_list")
    private List<ChoiceAnalyze> choiceAnalyzeList;

    @ManyToOne
    @JsonIgnore // 순환참조 방지
    @JoinColumn(name = "survey_analyze_id")
    private SurveyAnalyze surveyAnalyzeId;

}
