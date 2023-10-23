package com.example.surveydocument.survey.repository.surveyDocument;

import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.response.ManagementResponseDto;
import com.example.surveydocument.survey.response.QManagementResponseDto;
import com.example.surveydocument.survey.response.QSurveyPageDto;
import com.example.surveydocument.survey.response.SurveyPageDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.surveydocument.survey.domain.QDateManagement.dateManagement;
import static com.example.surveydocument.survey.domain.QDesign.design;
import static com.example.surveydocument.survey.domain.QQuestionDocument.questionDocument;
import static com.example.surveydocument.survey.domain.QSurveyDocument.surveyDocument;

@Transactional(readOnly = true)
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

    @Override
    public SurveyDocument findSurveyById(Long surveyDocumentId) {
        return queryFactory
                .selectFrom(surveyDocument)
                .leftJoin(surveyDocument.design, design).fetchJoin()
                .leftJoin(surveyDocument.date, dateManagement).fetchJoin()
                .leftJoin(surveyDocument.questionDocumentList, questionDocument).fetchJoin()
                .where(surveyDocument.id.eq(surveyDocumentId))
                .distinct()
                .fetchOne();
    }

    @Override
    public ManagementResponseDto findManageById(Long surveyDocumentId) {
        return queryFactory.select(new QManagementResponseDto(dateManagement.startDate, dateManagement.deadline, dateManagement.isEnabled))
                .from(dateManagement)
                .where(dateManagement.surveyDocument.id.eq(surveyDocumentId))
                .fetchOne();
    }

    @Override
    public Optional<SurveyDocument> findByIdToUpdate(Long surveyDocumentId) {
        SurveyDocument result = queryFactory
                .selectFrom(surveyDocument)
                .leftJoin(surveyDocument.design, design).fetchJoin()
                .leftJoin(surveyDocument.date, dateManagement).fetchJoin()
                .leftJoin(surveyDocument.questionDocumentList, questionDocument).fetchJoin()
                .where(surveyDocument.id.eq(surveyDocumentId))
                .distinct()
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    @Transactional
    public void updateManage(Long id, Boolean enable) {
        queryFactory.update(dateManagement)
                .where(dateManagement.surveyDocument.id.eq(id))
                .set(dateManagement.isEnabled, enable)
                .execute();
    }

    private OrderSpecifier<?> getOrder(String sortWhat, String sortHow) {
        if ("title".equals(sortWhat)) {
            return sortHow.equals("ascending") ? surveyDocument.title.asc() : surveyDocument.title.desc();
        } else {
            return sortHow.equals("ascending") ? surveyDocument.date.startDate.asc() : surveyDocument.date.startDate.desc();
        }
    }

}
