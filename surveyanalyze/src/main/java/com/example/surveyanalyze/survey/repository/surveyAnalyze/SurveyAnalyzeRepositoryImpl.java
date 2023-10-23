package com.example.surveyanalyze.survey.repository.surveyAnalyze;

import com.example.surveyanalyze.survey.domain.SurveyAnalyze;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public class SurveyAnalyzeRepositoryImpl implements SurveyAnalyzeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public SurveyAnalyzeRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    Optional<SurveyAnalyze> findBySurveyDocumentId(Long surveyDocumentId) {
//        queryFactory.
        return null;
    }

}
