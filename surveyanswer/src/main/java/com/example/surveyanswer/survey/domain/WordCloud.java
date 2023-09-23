package com.example.surveyanswer.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class WordCloud {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wordCloud_id")
    private Long id;
    @Column(name = "wordCloud_title")
    private String title;
    @Column(name = "wordCloud_count")
    private int count;

    @ManyToOne
    @JsonIgnore // 순환참조 방지
    @JoinColumn(name = "question_id")
    private QuestionDocument questionDocument;

    @Builder
    public WordCloud(String title, QuestionDocument questionDocument, int count) {
        this.title = title;
        this.questionDocument = questionDocument;
        this.count = count;
    }
}
