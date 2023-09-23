package com.example.surveyanswer.survey.repository.questionAnswer;

import com.example.surveyanswer.survey.domain.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long>, QuestionAnswerRepositoryCustom {
    List<QuestionAnswer> findQuestionAnswersBySurveyDocumentId(Long surveyDocumentId);

    List<QuestionAnswer> findQuestionAnswersByCheckAnswerId(Long questionId);
}

