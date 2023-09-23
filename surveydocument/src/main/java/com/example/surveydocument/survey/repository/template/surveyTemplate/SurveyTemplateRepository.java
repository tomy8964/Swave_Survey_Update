package com.example.surveydocument.survey.repository.template.surveyTemplate;

import com.example.surveydocument.survey.domain.SurveyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyTemplateRepository extends JpaRepository<SurveyTemplate, Long>, SurveyTemplateRepositoryCustom {
    SurveyTemplate findByTitle(String title);
}
