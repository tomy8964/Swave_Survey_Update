package com.example.surveyanswer.survey.controller;

import com.example.surveyanswer.survey.domain.SurveyAnswer;
import com.example.surveyanswer.survey.response.SurveyAnswerDto;
import com.example.surveyanswer.survey.response.SurveyDetailDto;
import com.example.surveyanswer.survey.response.SurveyResponseDto;
import com.example.surveyanswer.survey.service.SurveyAnswerService;
import lombok.RequiredArgsConstructor;
import org.redisson.RedissonRedLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/answer/external")
//@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class SurveyAnswerExternalController {

    private final SurveyAnswerService surveyService;
    private final RedissonClient redissonClient;

    // 설문 참여
    @GetMapping(value = "/load/{id}")
    public SurveyDetailDto participateSurvey(@PathVariable Long id) {
        return surveyService.getParticipantSurvey(id);
    }

    // 설문 응답 저장
    @PostMapping(value = "/response/create")
    @Caching(evict = {
            @CacheEvict(value = "responseList", key = "'responseList-' + #surveyForm.id", cacheManager = "cacheManager" ),
            @CacheEvict(value = "getQuestionAnswerByCheckAnswerId", allEntries = true, cacheManager = "cacheManager" )
    })
    public void createResponse(@RequestBody SurveyResponseDto surveyForm) {
        surveyService.createSurveyAnswer(surveyForm);
//
//        RedissonRedLock lock = new RedissonRedLock(redissonClient.getLock("$surveyDocumentId"));
//
//        try {
//            if (lock.tryLock(1, 3, TimeUnit.SECONDS)) {
//                // transaction
//                return "Success";
//            } else {
//                throw new RuntimeException("Failed to acquire lock.");
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            lock.unlock();
//        }
    }

    // 설문 응답들 조회
    @GetMapping(value = "/response/{id}")
    @Cacheable(value = "responseList", key = "'responseList-' + #id", cacheManager = "cacheManager" )
    public List<SurveyAnswerDto> readResponse(@PathVariable Long id){
        return surveyService.getSurveyAnswersBySurveyDocumentId(id);
    }

}
