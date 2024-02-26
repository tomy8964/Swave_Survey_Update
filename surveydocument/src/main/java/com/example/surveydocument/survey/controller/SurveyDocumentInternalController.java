package com.example.surveydocument.survey.controller;

import com.example.surveydocument.survey.response.ChoiceDetailDto;
import com.example.surveydocument.survey.response.QuestionDetailDto;
import com.example.surveydocument.survey.response.SurveyDetailDto;
import com.example.surveydocument.survey.response.SurveyDetailDto2;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<SurveyDetailDto> readDetail1(@PathVariable Long id) {
        return new ResponseEntity<>(surveyService.readSurveyDetail(id), HttpStatus.OK);
    }

    @GetMapping(value = "/getSurveyDocumentToAnalyze/{id}")
    @Cacheable(value = "survey2", key = "'survey2-' + #id", cacheManager = "cacheManager")
    public ResponseEntity<SurveyDetailDto2> readDetail2(@PathVariable Long id) {
        return new ResponseEntity<>(surveyService.readSurveyDetail2(id), HttpStatus.OK);
    }

    @PostMapping(value = "/count/{id}")
    public ResponseEntity<Long> countChoice(@PathVariable Long id) {
        return new ResponseEntity<>(surveyService.countChoice(id), HttpStatus.NO_CONTENT);
    }

    // Survey Document 응답자 ++
    @PostMapping(value = "/countAnswer/{surveyDocumentId}")
    public ResponseEntity<Long> countAnswer(@PathVariable Long surveyDocumentId) {
        return new ResponseEntity<>(surveyService.countSurveyDocument(surveyDocumentId), HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/getChoice/{id}")
    @Cacheable(value = "choice", key = "'choice-' + #id", cacheManager = "cacheManager")
    public ResponseEntity<ChoiceDetailDto> getChoice(@PathVariable Long id) {
        return new ResponseEntity<>(surveyService.getChoice(id), HttpStatus.OK);
    }

    @GetMapping(value = "/getQuestion/{id}")
    @Cacheable(value = "question", key = "'question-' + #id", cacheManager = "cacheManager")
    public ResponseEntity<QuestionDetailDto> getQuestion(@PathVariable Long id) {
        return new ResponseEntity<>(surveyService.getQuestion(id), HttpStatus.OK);
    }

    @GetMapping(value = "/getQuestionByChoiceId/{id}")
    @Cacheable(value = "getQuestionByChoiceId", key = "'choiceByquestion-' + #id", cacheManager = "cacheManager")
    public ResponseEntity<QuestionDetailDto> getQuestionByChoiceId(@PathVariable Long id) {
        return new ResponseEntity<>(surveyService.getQuestionByChoiceId(id), HttpStatus.OK);
    }

}
