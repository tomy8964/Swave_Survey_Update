package com.example.surveyanalyze.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Choice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "choice_id")
    private Long id;
    @Column(name = "choice_title")
    private String title;
    @Column(name = "choice_count")
    private int count;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "question_id")
    private QuestionDocument question_id;

    @Builder
    public Choice(String title, QuestionDocument question_id, int count) {
        this.title = title;
        this.question_id = question_id;
        this.count = count;
    }
}
