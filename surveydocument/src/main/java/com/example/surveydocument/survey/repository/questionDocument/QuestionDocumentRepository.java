package com.example.surveydocument.survey.repository.questionDocument;

import com.example.surveydocument.survey.domain.QuestionDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionDocumentRepository extends JpaRepository<QuestionDocument, Long>, QuestionDocumentRepositoryCustom {
}
