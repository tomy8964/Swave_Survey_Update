package com.example.surveydocument.survey.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@NoArgsConstructor
@Entity(name = "wordCloud")
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE wordCloud SET is_deleted = true WHERE wordCloud_id = ?")
public class WordCloud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wordCloud_id")
    private Long id;
    @Column(name = "wordCloud_title")
    private String title;
    @Column(name = "wordCloud_count")
    private int count;

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private QuestionDocument questionDocument;

    public WordCloud(Long id, String title, int count, boolean isDeleted, QuestionDocument questionDocument) {
        this.id = id;
        this.title = title;
        this.count = count;
        this.isDeleted = isDeleted;
        this.questionDocument = questionDocument;
    }
}
