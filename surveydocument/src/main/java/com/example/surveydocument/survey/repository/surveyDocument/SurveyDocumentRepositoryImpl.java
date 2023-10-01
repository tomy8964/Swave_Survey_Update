package com.example.surveydocument.survey.repository.surveyDocument;

import com.example.surveydocument.survey.domain.QSurveyDocument;
import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.response.QSurveyPageDto;
import com.example.surveydocument.survey.response.SurveyPageDto;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.example.surveydocument.survey.domain.QSurveyDocument.surveyDocument;

public class SurveyDocumentRepositoryImpl implements SurveyDocumentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public SurveyDocumentRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<SurveyPageDto> pagingSurvey(Long userId, String sortWhat, String sortHow, Pageable pageable) {



        List<SurveyPageDto> content = queryFactory
                .select(new QSurveyPageDto(
                                surveyDocument.title,
                                surveyDocument.date.startDate
                        )
                )
                .from(surveyDocument)
                .where(surveyDocument.userId.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrder(sortWhat, sortHow))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(surveyDocument.count())
                .from(surveyDocument)
                .where(surveyDocument.userId.eq(userId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier<?> getOrder(String sortWhat, String sortHow) {
        if ("title".equals(sortWhat)) {
            return sortHow.equals("ascending") ? surveyDocument.title.asc() : surveyDocument.title.desc();
        } else {
            return sortHow.equals("ascending") ? surveyDocument.date.startDate.asc() : surveyDocument.date.startDate.desc();
        }
    }

}
