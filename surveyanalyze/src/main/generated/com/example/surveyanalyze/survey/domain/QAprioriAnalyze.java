package com.example.surveyanalyze.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAprioriAnalyze is a Querydsl query type for AprioriAnalyze
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAprioriAnalyze extends EntityPathBase<AprioriAnalyze> {

    private static final long serialVersionUID = 132868591L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAprioriAnalyze aprioriAnalyze = new QAprioriAnalyze("aprioriAnalyze");

    public final ListPath<ChoiceAnalyze, QChoiceAnalyze> choiceAnalyzeList = this.<ChoiceAnalyze, QChoiceAnalyze>createList("choiceAnalyzeList", ChoiceAnalyze.class, QChoiceAnalyze.class, PathInits.DIRECT2);

    public final NumberPath<Long> choiceId = createNumber("choiceId", Long.class);

    public final StringPath choiceTitle = createString("choiceTitle");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath questionTitle = createString("questionTitle");

    public final QSurveyAnalyze surveyAnalyze;

    public QAprioriAnalyze(String variable) {
        this(AprioriAnalyze.class, forVariable(variable), INITS);
    }

    public QAprioriAnalyze(Path<? extends AprioriAnalyze> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAprioriAnalyze(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAprioriAnalyze(PathMetadata metadata, PathInits inits) {
        this(AprioriAnalyze.class, metadata, inits);
    }

    public QAprioriAnalyze(Class<? extends AprioriAnalyze> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.surveyAnalyze = inits.isInitialized("surveyAnalyze") ? new QSurveyAnalyze(forProperty("surveyAnalyze")) : null;
    }

}

