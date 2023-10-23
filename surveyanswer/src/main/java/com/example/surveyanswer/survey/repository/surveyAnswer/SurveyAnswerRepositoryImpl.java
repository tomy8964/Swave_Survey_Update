package com.example.surveyanswer.survey.repository.surveyAnswer;

import com.example.surveyanswer.survey.domain.SurveyAnswer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.surveyanswer.survey.domain.QQuestionAnswer.questionAnswer;
import static com.example.surveyanswer.survey.domain.QSurveyAnswer.surveyAnswer;

@Transactional(readOnly = true)
public class SurveyAnswerRepositoryImpl implements SurveyAnswerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public SurveyAnswerRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<SurveyAnswer> findSurveyAnswerListBySurveyDocumentId(Long surveyId) {
        return queryFactory
                .selectFrom(surveyAnswer)
                .leftJoin(surveyAnswer.questionAnswersList, questionAnswer).fetchJoin()
                .where(surveyAnswer.surveyDocumentId.eq(surveyId))
                .distinct()
                .fetch();
    }
}
