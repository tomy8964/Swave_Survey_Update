package com.example.surveyanswer.survey.controller;

import com.example.surveyanswer.survey.domain.QuestionAnswer;
import com.example.surveyanswer.survey.response.QuestionAnswerDto;
import com.example.surveyanswer.survey.service.SurveyAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/answer/internal")
//@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class SurveyAnswerInternalController {

    private final SurveyAnswerService surveyService;

    @Cacheable(value = "getQuestionAnswerByCheckAnswerId", key = "#id", cacheManager = "cacheManager")
    @GetMapping(value = "/getQuestionAnswerByCheckAnswerId/{id}")
    public List<QuestionAnswerDto> getQuestionAnswerByCheckAnswerId(@PathVariable Long id) {
        return surveyService.getQuestionAnswerByCheckAnswerId(id);
    }
}
