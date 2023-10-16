package com.example.surveyanalyze.survey.controller;

import com.example.surveyanalyze.survey.response.SurveyAnalyzeDto;
import com.example.surveyanalyze.survey.response.SurveyDetailDto;
import com.example.surveyanalyze.survey.service.SurveyAnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/analyze/external")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class SurveyAnalyzeExternalController {

    private final SurveyAnalyzeService surveyService;

    // 분석 문항 & 응답
    @GetMapping(value = "/research/survey/load/{id}")
    public SurveyDetailDto readSurvey(@PathVariable Long id) {
        return surveyService.getSurveyDetailDto(id);
    }

    // 설문 상세 분석 조회
    @GetMapping(value = "/research/analyze/{surveydocumentId}")
    @Cacheable(value = "surveyAnalyze", key = "'surveyAnalyze-' + #surveydocumentId", cacheManager = "cacheManager" )
    public SurveyAnalyzeDto readDetailAnalyze(@PathVariable Long surveydocumentId) {
        return surveyService.readSurveyDetailAnalyze(surveydocumentId);
    }
}
