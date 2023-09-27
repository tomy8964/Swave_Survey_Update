package com.example.surveyanalyze.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCompareAnalyze is a Querydsl query type for CompareAnalyze
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCompareAnalyze extends EntityPathBase<CompareAnalyze> {

    private static final long serialVersionUID = -849101846L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCompareAnalyze compareAnalyze = new QCompareAnalyze("compareAnalyze");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> pValue = createNumber("pValue", Double.class);

    public final QQuestionAnalyze questionAnalyze;

    public final StringPath questionTitle = createString("questionTitle");

    public QCompareAnalyze(String variable) {
        this(CompareAnalyze.class, forVariable(variable), INITS);
    }

    public QCompareAnalyze(Path<? extends CompareAnalyze> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCompareAnalyze(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCompareAnalyze(PathMetadata metadata, PathInits inits) {
        this(CompareAnalyze.class, metadata, inits);
    }

    public QCompareAnalyze(Class<? extends CompareAnalyze> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.questionAnalyze = inits.isInitialized("questionAnalyze") ? new QQuestionAnalyze(forProperty("questionAnalyze"), inits.get("questionAnalyze")) : null;
    }

}

