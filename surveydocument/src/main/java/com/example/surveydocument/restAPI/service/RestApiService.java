package com.example.surveydocument.restAPI.service;

import com.example.surveydocument.survey.response.QuestionAnswerDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

public interface RestApiService {
    Optional<Long> getCurrentUserFromJWTToken(HttpServletRequest request);

    List<QuestionAnswerDto> getQuestionAnswersByCheckAnswerId(Long id);
}
