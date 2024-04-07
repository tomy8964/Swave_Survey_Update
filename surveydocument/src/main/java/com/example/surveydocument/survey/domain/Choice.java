package com.example.surveydocument.survey.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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

    public void addCount() {
        this.count++;
    }
}
