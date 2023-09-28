package com.example.surveydocument.survey.repository.surveyDocument;

import com.example.surveydocument.survey.domain.SurveyDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyDocumentRepository extends JpaRepository<SurveyDocument, Long>, SurveyDocumentRepositoryCustom {

}
