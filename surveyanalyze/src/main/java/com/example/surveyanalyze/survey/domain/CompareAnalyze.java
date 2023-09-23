package com.example.surveyanalyze.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
public class CompareAnalyze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compare_analyze_id")
    private Long id;
    @Column(name = "question_title")
    private String questionTitle;
    @Column(name = "p_value")
    private Double pValue;

    @ManyToOne
    @JsonIgnore // 순환참조 방지
    @JoinColumn(name = "question_analyze_id")
    private QuestionAnalyze questionAnalyzeId;
}
