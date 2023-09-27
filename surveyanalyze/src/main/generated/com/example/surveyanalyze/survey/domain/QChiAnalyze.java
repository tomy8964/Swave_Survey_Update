package com.example.surveyanalyze.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChiAnalyze is a Querydsl query type for ChiAnalyze
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChiAnalyze extends EntityPathBase<ChiAnalyze> {

    private static final long serialVersionUID = 1095281867L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChiAnalyze chiAnalyze = new QChiAnalyze("chiAnalyze");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> pValue = createNumber("pValue", Double.class);

    public final QQuestionAnalyze questionAnalyze;

    public final StringPath questionTitle = createString("questionTitle");

    public QChiAnalyze(String variable) {
        this(ChiAnalyze.class, forVariable(variable), INITS);
    }

    public QChiAnalyze(Path<? extends ChiAnalyze> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChiAnalyze(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChiAnalyze(PathMetadata metadata, PathInits inits) {
        this(ChiAnalyze.class, metadata, inits);
    }

    public QChiAnalyze(Class<? extends ChiAnalyze> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.questionAnalyze = inits.isInitialized("questionAnalyze") ? new QQuestionAnalyze(forProperty("questionAnalyze"), inits.get("questionAnalyze")) : null;
    }

}

