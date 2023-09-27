package com.example.surveyanswer.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSurveyAnswer is a Querydsl query type for SurveyAnswer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSurveyAnswer extends EntityPathBase<SurveyAnswer> {

    private static final long serialVersionUID = 1778011475L;

    public static final QSurveyAnswer surveyAnswer = new QSurveyAnswer("surveyAnswer");

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<QuestionAnswer, QQuestionAnswer> questionAnswersList = this.<QuestionAnswer, QQuestionAnswer>createList("questionAnswersList", QuestionAnswer.class, QQuestionAnswer.class, PathInits.DIRECT2);

    public final NumberPath<Long> surveyDocumentId = createNumber("surveyDocumentId", Long.class);

    public final StringPath title = createString("title");

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public QSurveyAnswer(String variable) {
        super(SurveyAnswer.class, forVariable(variable));
    }

    public QSurveyAnswer(Path<? extends SurveyAnswer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSurveyAnswer(PathMetadata metadata) {
        super(SurveyAnswer.class, metadata);
    }

}

