package com.example.surveydocument.survey.repository.surveyDocument;

import com.example.surveydocument.survey.domain.SurveyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SurveyDocumentRepository extends JpaRepository<SurveyDocument, Long>, QuerydslPredicateExecutor<SurveyDocument>, SurveyDocumentRepositoryCustom {

    @Modifying
    @Transactional
    @Query("UPDATE SurveyDocument sd SET sd.countAnswer = sd.countAnswer + 1 WHERE sd.id = :id")
    void incrementCountAnswer(@Param("id") Long id);
}
