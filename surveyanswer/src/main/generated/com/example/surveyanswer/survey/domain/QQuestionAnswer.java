package com.example.surveyanswer.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuestionAnswer is a Querydsl query type for QuestionAnswer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestionAnswer extends EntityPathBase<QuestionAnswer> {

    private static final long serialVersionUID = 1323257375L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuestionAnswer questionAnswer = new QQuestionAnswer("questionAnswer");

    public final StringPath checkAnswer = createString("checkAnswer");

    public final NumberPath<Long> checkAnswerId = createNumber("checkAnswerId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> questionType = createNumber("questionType", Integer.class);

    public final QSurveyAnswer surveyAnswer;

    public final NumberPath<Long> surveyDocumentId = createNumber("surveyDocumentId", Long.class);

    public final StringPath title = createString("title");

    public QQuestionAnswer(String variable) {
        this(QuestionAnswer.class, forVariable(variable), INITS);
    }

    public QQuestionAnswer(Path<? extends QuestionAnswer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuestionAnswer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuestionAnswer(PathMetadata metadata, PathInits inits) {
        this(QuestionAnswer.class, metadata, inits);
    }

    public QQuestionAnswer(Class<? extends QuestionAnswer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.surveyAnswer = inits.isInitialized("surveyAnswer") ? new QSurveyAnswer(forProperty("surveyAnswer")) : null;
    }

}

