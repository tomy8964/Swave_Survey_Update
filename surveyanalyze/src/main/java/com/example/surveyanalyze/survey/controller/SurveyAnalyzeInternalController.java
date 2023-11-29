package com.example.surveyanalyze.survey.controller;

import com.example.surveyanalyze.survey.service.SurveyAnalyzeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/analyze/internal")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class SurveyAnalyzeInternalController {

    private final SurveyAnalyzeService surveyService;

    @Autowired
    public SurveyAnalyzeInternalController(SurveyAnalyzeService surveyService) {
        this.surveyService = surveyService;
    }

    // 설문 분석 시작
    @Transactional
    @PostMapping(value = "/research/analyze/create")
    @CacheEvict(value = "surveyAnalyze", key = "'surveyAnalyze-' + #surveyId", cacheManager = "cacheManager" )
    public String saveAnalyze(@RequestBody String surveyId) {
        surveyService.analyze(surveyId);
        surveyService.wordCloudPython(surveyId);
        return "success";
    }
}
