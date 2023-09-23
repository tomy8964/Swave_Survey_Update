package com.example.surveyanswer.survey.controller;

import com.example.surveyanswer.survey.domain.QuestionAnswer;
import com.example.surveyanswer.survey.response.SurveyDetailDto;
import com.example.surveyanswer.survey.response.SurveyResponseDto;
import com.example.surveyanswer.survey.service.SurveyAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/answer/internal")
//@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class SurveyAnswerInternalController {

    private final SurveyAnswerService surveyService;

    // getQuestionAnswer
//    @Cacheable(value = "getQuestionAnswer", key = "#id")
    @GetMapping(value = "/question/list/{id}")
    public List<QuestionAnswer> getQuestionAnswers(@PathVariable Long id){
        return surveyService.getQuestionAnswers(id);
    }

    // getQuestionAnswerByCheckAnswerId
//    @Cacheable(value = "getQuestionAnswerByCheckAnswerId", key = "#id")
    @GetMapping(value = "/getQuestionAnswerByCheckAnswerId/{id}")
    public List<QuestionAnswer> getQuestionAnswerByCheckAnswerId(@PathVariable Long id){
        return surveyService.getQuestionAnswerByCheckAnswerId(id);
    }
}
