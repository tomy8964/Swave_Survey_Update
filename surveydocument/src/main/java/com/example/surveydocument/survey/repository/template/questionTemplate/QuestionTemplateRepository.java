package com.example.surveydocument.survey.repository.template.questionTemplate;

import com.example.surveydocument.survey.domain.QuestionDocument;
import com.example.surveydocument.survey.domain.QuestionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionTemplateRepository extends JpaRepository<QuestionTemplate, Long>, QuestionTemplateRepositoryCustom {
}
