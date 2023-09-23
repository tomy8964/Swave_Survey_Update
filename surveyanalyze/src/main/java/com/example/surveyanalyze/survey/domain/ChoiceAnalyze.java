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
public class ChoiceAnalyze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "choice_analyze_id")
    private Long id;
    @Column(name = "support", nullable = false)
    private double support;
    //하나만 가져옴
    @Column(name = "연관분석된_choice_id")
    private long choiceId;
    @Column(name = "question_title")
    private String questionTitle;
    @Column(name = "choice_title")
    private String choiceTitle;


    @ManyToOne
    @JsonIgnore // 순환참조 방지
    @JoinColumn(name = "apriori_analyze_id")
    private AprioriAnalyze aprioriAnalyzeId;

}
