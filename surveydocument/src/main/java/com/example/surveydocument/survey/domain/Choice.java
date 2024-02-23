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
@SQLDelete(sql = "UPDATE choice SET is_deleted = true WHERE choice_id = ?")
public class Choice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "choice_id")
    private Long id;
    @Column(name = "choice_title")
    private String title;
    @Builder.Default
    @Column(name = "choice_count")
    private int count = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private QuestionDocument questionDocument;

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = Boolean.FALSE;

    public Choice(Long id, String title, int count, QuestionDocument questionDocument, boolean isDeleted) {
        this.id = id;
        this.title = title;
        this.count = count;
        if (questionDocument != null) {
            this.questionDocument = questionDocument;
            questionDocument.getChoiceList().add(this);
        }
        this.isDeleted = isDeleted;
    }

    public void addCount() {
        this.count++;
    }
}
