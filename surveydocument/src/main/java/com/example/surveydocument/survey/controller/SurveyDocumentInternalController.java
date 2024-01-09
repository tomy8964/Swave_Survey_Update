package com.example.surveydocument.survey.controller;

import com.example.surveydocument.survey.response.ChoiceDetailDto;
import com.example.surveydocument.survey.response.QuestionDetailDto;
import com.example.surveydocument.survey.response.SurveyDetailDto;
import com.example.surveydocument.survey.response.SurveyDetailDto2;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@RequestMapping("/api/document/internal")
public class SurveyDocumentInternalController {

    private final SurveyDocumentService surveyService;

    @GetMapping(value = "/getSurveyDocumentToAnswer/{id}")
    @Cacheable(value = "survey", key = "'survey-' + #id", cacheManager = "cacheManager")
    public SurveyDetailDto readDetail1(@PathVariable Long id) {
        return surveyService.readSurveyDetail(id);
    }

    @GetMapping(value = "/getSurveyDocumentToAnalyze/{id}")
    @Cacheable(value = "survey2", key = "'survey2-' + #id", cacheManager = "cacheManager")
    public SurveyDetailDto2 readDetail2(@PathVariable Long id) {
        return surveyService.readSurveyDetail2(id);
    }

    @PostMapping(value = "/count/{id}")
    public void countChoice(@PathVariable Long id) {
        try {
            surveyService.countChoice(id);
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
        }
    }

    // Survey Document 응답자 ++
    @PostMapping(value = "/countAnswer/{id}")
    public void countAnswer(@PathVariable Long id) throws Exception {
        surveyService.countSurveyDocument(id);
    }

    @GetMapping(value = "/getChoice/{id}")
    @Cacheable(value = "choice", key = "'choice-' + #id", cacheManager = "cacheManager")
    public ChoiceDetailDto getChoice(@PathVariable Long id) {
        return surveyService.getChoice(id);
    }

    @GetMapping(value = "/getQuestion/{id}")
    @Cacheable(value = "question", key = "'question-' + #id", cacheManager = "cacheManager")
    public QuestionDetailDto getQuestion(@PathVariable Long id) {

        return surveyService.getQuestion(id);
    }

    @GetMapping(value = "/getQuestionByChoiceId/{id}")
    @Cacheable(value = "getQuestionByChoiceId", key = "'choiceByquestion-' + #id", cacheManager = "cacheManager")
    public QuestionDetailDto getQuestionByChoiceId(@PathVariable Long id) {
        return surveyService.getQuestionByChoiceId(id);
    }

}
