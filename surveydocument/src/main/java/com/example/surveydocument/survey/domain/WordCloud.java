package com.example.surveydocument.survey.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Entity
@Builder
@NoArgsConstructor
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE wordCloud SET is_deleted = true WHERE wordCloud_id = ?")
public class WordCloud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wordCloud_id")
    private Long id;
    @Column(name = "wordCloud_title")
    private String title;

    @Builder.Default
    @Column(name = "wordCloud_count")
    private int count = 0;

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private QuestionDocument questionDocument;

    @Builder
    public WordCloud(Long id, String title, int count, boolean isDeleted, QuestionDocument questionDocument) {
        this.id = id;
        this.title = title;
        this.count = count;
        if (questionDocument != null) {
            this.questionDocument = questionDocument;
            questionDocument.getWordCloudList().add(this);
        }
        this.isDeleted = isDeleted;
    }
}
