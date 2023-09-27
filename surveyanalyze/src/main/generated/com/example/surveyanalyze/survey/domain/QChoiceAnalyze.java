package com.example.surveyanalyze.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChoiceAnalyze is a Querydsl query type for ChoiceAnalyze
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChoiceAnalyze extends EntityPathBase<ChoiceAnalyze> {

    private static final long serialVersionUID = 450976968L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChoiceAnalyze choiceAnalyze = new QChoiceAnalyze("choiceAnalyze");

    public final QAprioriAnalyze aprioriAnalyze;

    public final NumberPath<Long> choiceId = createNumber("choiceId", Long.class);

    public final StringPath choiceTitle = createString("choiceTitle");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath questionTitle = createString("questionTitle");

    public final NumberPath<Double> support = createNumber("support", Double.class);

    public QChoiceAnalyze(String variable) {
        this(ChoiceAnalyze.class, forVariable(variable), INITS);
    }

    public QChoiceAnalyze(Path<? extends ChoiceAnalyze> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChoiceAnalyze(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChoiceAnalyze(PathMetadata metadata, PathInits inits) {
        this(ChoiceAnalyze.class, metadata, inits);
    }

    public QChoiceAnalyze(Class<? extends ChoiceAnalyze> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.aprioriAnalyze = inits.isInitialized("aprioriAnalyze") ? new QAprioriAnalyze(forProperty("aprioriAnalyze"), inits.get("aprioriAnalyze")) : null;
    }

}

