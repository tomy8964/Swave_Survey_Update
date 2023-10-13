package com.example.surveydocument.survey.controller;

import com.example.surveydocument.survey.response.ChoiceDetailDto;
import com.example.surveydocument.survey.response.QuestionDetailDto;
import com.example.surveydocument.survey.response.SurveyDetailDto;
import com.example.surveydocument.survey.response.SurveyDetailDto2;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@RequestMapping("/api/document/internal")
public class SurveyDocumentInternalController {

    private final SurveyDocumentService surveyService;

    @Cacheable(value = "survey", key = "'survey-' + #id", cacheManager = "cacheManager" )
    @GetMapping(value = "/getSurveyDocument/{id}")
    public SurveyDetailDto readDetail1(@PathVariable Long id) {
        return surveyService.readSurveyDetail(id);
    }

    @GetMapping(value = "/getSurveyDocument2/{id}")
    public SurveyDetailDto2 readDetail2(@PathVariable Long id) {
        return surveyService.readSurveyDetail2(id);
    }

    @PostMapping(value = "/count/{id}")
    public void countChoice(@PathVariable Long id) {
        surveyService.countChoice(id);
    }

    // Survey Document 응답자 ++
    @PostMapping(value = "/countAnswer/{id}")
    public void countAnswer(@PathVariable Long id) throws Exception {
        surveyService.countSurveyDocument(id);
    }

//    @PostMapping(value = "/setWordCloud/{id}")
//    public void setWordCloud(@PathVariable Long id, @RequestBody List<WordCloudDto> wordCloudList) {
//        RedissonRedLock lock = new RedissonRedLock(redissonClient.getLock("$surveyDocumentId"));
//
//        try {
//            if (lock.tryLock(1, 3, TimeUnit.SECONDS)) {
//                // transaction
//                surveyService.setWordCloud(id, wordCloudList);
//            } else {
//                throw new RuntimeException("Failed to acquire lock.");
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            lock.unlock();
//        }

    @Cacheable(value = "choice", key = "'choice-' + #id", cacheManager = "cacheManager" )
    @GetMapping(value = "/getChoice/{id}")
    public ChoiceDetailDto getChoice(@PathVariable Long id) {
        return surveyService.getChoice(id);
    }

    @Cacheable(value = "question", key = "'question-' + #id", cacheManager = "cacheManager" )
    @GetMapping(value = "/getQuestion/{id}")
    public QuestionDetailDto getQuestion(@PathVariable Long id) {

        return surveyService.getQuestion(id);
    }

    @Cacheable(value = "getQuestionByChoiceId", key = "'choiceByquestion-' + #id", cacheManager = "cacheManager" )
    @GetMapping(value = "/getQuestionByChoiceId/{id}")
    public QuestionDetailDto getQuestionByChoiceId(@PathVariable Long id) {
        return surveyService.getQuestionByChoiceId(id);
    }

}
