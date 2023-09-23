package com.example.surveydocument.survey.repository.survey;

import com.example.surveydocument.survey.domain.QDateManagement;
import com.example.surveydocument.survey.domain.QSurvey;
import com.example.surveydocument.survey.domain.QSurveyDocument;
import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.request.PageRequestDto;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class SurveyRepositoryImpl implements SurveyRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;

    @PersistenceContext
    EntityManager entityManager;

    // survey document 의 list 를 페이징 처리하여 조회
    @Override
    public Page<SurveyDocument> surveyDocumentPaging(Long userRequest, Pageable pageable) {
        List<SurveyDocument> results = getSurveyDocumentList(userRequest, pageable);

        QSurveyDocument surveyDocument = QSurveyDocument.surveyDocument;

        Long count = jpaQueryFactory
                .select(surveyDocument.count())
                .from(surveyDocument)
                .where(surveyDocument.survey.userCode.eq(userRequest))
                .fetchOne();

        return new PageImpl<>(results, pageable, count);
    }

    public List<SurveyDocument> getSurveyDocumentList(Long userRequest, Pageable pageable) {
        QSurveyDocument surveyDocument = QSurveyDocument.surveyDocument;
        QSurvey survey = QSurvey.survey;

        List<SurveyDocument> result = jpaQueryFactory
                .select(surveyDocument)
                .from(surveyDocument)
                .where(surveyDocument.survey.userCode.eq(userRequest))
                .orderBy(SurveyDocumentSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return result;
    }

    // survey Document 를 gird 형식으로 조회할 때 페이징 처리 없이 모두 다 조회
    public List<SurveyDocument> getSurveyDocumentListGrid(Long userRequest, PageRequestDto pageRequest) {
        QSurveyDocument surveyDocument = QSurveyDocument.surveyDocument;

        String sort1 = pageRequest.getSort1(); // date or title
        String sort2 = pageRequest.getSort2(); // ascending or descending
        return jpaQueryFactory
                .select(surveyDocument)
                .from(surveyDocument)
                .where(surveyDocument.survey.userCode.eq(userRequest))
                .orderBy(SurveyDocumentSort(sort1, sort2))
                .fetch();
    }

    // survey document 상세 조회
    @Override
    public SurveyDocument surveyDocumentDetail(Long userRequest, SurveyDocument surveyDocumentRequest) {
        QSurvey survey = QSurvey.survey;
        QSurveyDocument surveyDocument = QSurveyDocument.surveyDocument;

        SurveyDocument result = (SurveyDocument) jpaQueryFactory
                .select(survey.surveyDocumentList)
                .from(survey)
                .where(survey.userCode.eq(userRequest)
                        .and(survey.surveyDocumentList.contains(surveyDocumentRequest)))
                .fetch();
        return null;
    }

    // 설문 참여자 수 증가
    public void surveyDocumentCount(SurveyDocument surveyDocument) {
        QSurveyDocument qSurveyDocument = QSurveyDocument.surveyDocument;

        jpaQueryFactory
                .update(qSurveyDocument)
                .set(qSurveyDocument.countAnswer, qSurveyDocument.countAnswer.add(1))
                .where(qSurveyDocument.id.eq(surveyDocument.getId()))
                .execute();
        entityManager.flush();
        entityManager.clear();
    }

    // 특정 기준(날짜 or 오름차순)에 따라 정렬
    private OrderSpecifier<?> SurveyDocumentSort(String sort1, String sort2) {
        QSurveyDocument surveyDocument = QSurveyDocument.surveyDocument;

        Order direction;
        OrderSpecifier orderDirection;
        // Ascending or Descending
        if(sort2.equals("ascending")) {
            direction = Order.ASC;
            orderDirection = getOrderSpecifier(surveyDocument, sort1, direction);
        }
        else {
            direction = Order.DESC;
            orderDirection = getOrderSpecifier(surveyDocument, sort1, direction);
        }
        return orderDirection;
    }

    private OrderSpecifier<?> SurveyDocumentSort(Pageable page) {
        QSurveyDocument surveyDocument = QSurveyDocument.surveyDocument;

        OrderSpecifier orderDirection = null;
        if (!page.getSort().isEmpty()) {
            //정렬값이 들어 있으면 for 사용하여 값을 가져온다
            for (Sort.Order order : page.getSort()) {
                // 서비스에서 넣어준 DESC or ASC 를 가져온다.
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
                // 서비스에서 넣어준 정렬 조건을 스위치 케이스 문을 활용하여 셋팅하여 준다.
                orderDirection = getOrderSpecifier(surveyDocument, order.getProperty(), direction);
            }
        }
        return orderDirection;
    }

    private static OrderSpecifier getOrderSpecifier(QSurveyDocument surveyDocument, String property, Order direction) {
        QDateManagement date = QDateManagement.dateManagement;

        switch (property){
            case "date" :
                log.info("시작 날짜 순으로 정렬");
                return new OrderSpecifier(direction, date.startDate);
            case "title":
                log.info("제목 순으로 정렬");
                return new OrderSpecifier(direction, surveyDocument.title);
        }
        return null;
    }
}