package com.example.surveydocument.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ChoiceTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "choice_template_id")
    private Long id;
    @Column(name = "choice_title")
    private String title;
    @Column(name = "choice_count")
    private int count;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "question_template_id")
    private QuestionTemplate question_template_id;

    @Builder
    public ChoiceTemplate(String title, QuestionTemplate question_template_id,  int count) {
        this.title = title;
        this.question_template_id = question_template_id;
        this.count = count;
    }
}
