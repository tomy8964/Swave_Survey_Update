package com.example.surveyanalyze.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuestionAnalyze is a Querydsl query type for QuestionAnalyze
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestionAnalyze extends EntityPathBase<QuestionAnalyze> {

    private static final long serialVersionUID = 66991011L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuestionAnalyze questionAnalyze = new QQuestionAnalyze("questionAnalyze");

    public final ListPath<ChiAnalyze, QChiAnalyze> chiAnalyzeList = this.<ChiAnalyze, QChiAnalyze>createList("chiAnalyzeList", ChiAnalyze.class, QChiAnalyze.class, PathInits.DIRECT2);

    public final ListPath<CompareAnalyze, QCompareAnalyze> compareAnalyzeList = this.<CompareAnalyze, QCompareAnalyze>createList("compareAnalyzeList", CompareAnalyze.class, QCompareAnalyze.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath questionTitle = createString("questionTitle");

    public final QSurveyAnalyze surveyAnalyze;

    public final StringPath wordCloud = createString("wordCloud");

    public QQuestionAnalyze(String variable) {
        this(QuestionAnalyze.class, forVariable(variable), INITS);
    }

    public QQuestionAnalyze(Path<? extends QuestionAnalyze> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuestionAnalyze(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuestionAnalyze(PathMetadata metadata, PathInits inits) {
        this(QuestionAnalyze.class, metadata, inits);
    }

    public QQuestionAnalyze(Class<? extends QuestionAnalyze> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.surveyAnalyze = inits.isInitialized("surveyAnalyze") ? new QSurveyAnalyze(forProperty("surveyAnalyze")) : null;
    }

}

