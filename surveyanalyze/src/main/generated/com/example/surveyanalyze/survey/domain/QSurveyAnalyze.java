package com.example.surveyanalyze.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSurveyAnalyze is a Querydsl query type for SurveyAnalyze
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSurveyAnalyze extends EntityPathBase<SurveyAnalyze> {

    private static final long serialVersionUID = -744464145L;

    public static final QSurveyAnalyze surveyAnalyze = new QSurveyAnalyze("surveyAnalyze");

    public final ListPath<AprioriAnalyze, QAprioriAnalyze> aprioriAnalyzeList = this.<AprioriAnalyze, QAprioriAnalyze>createList("aprioriAnalyzeList", AprioriAnalyze.class, QAprioriAnalyze.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<QuestionAnalyze, QQuestionAnalyze> questionAnalyzeList = this.<QuestionAnalyze, QQuestionAnalyze>createList("questionAnalyzeList", QuestionAnalyze.class, QQuestionAnalyze.class, PathInits.DIRECT2);

    public final NumberPath<Long> surveyDocumentId = createNumber("surveyDocumentId", Long.class);

    public QSurveyAnalyze(String variable) {
        super(SurveyAnalyze.class, forVariable(variable));
    }

    public QSurveyAnalyze(Path<? extends SurveyAnalyze> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSurveyAnalyze(PathMetadata metadata) {
        super(SurveyAnalyze.class, metadata);
    }

}

