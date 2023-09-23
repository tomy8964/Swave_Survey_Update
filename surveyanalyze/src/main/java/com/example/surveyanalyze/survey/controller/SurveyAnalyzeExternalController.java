package com.example.surveyanalyze.survey.controller;

import com.example.surveyanalyze.survey.exception.InvalidTokenException;
import com.example.surveyanalyze.survey.response.SurveyAnalyzeDto;
import com.example.surveyanalyze.survey.response.SurveyDetailDto;
import com.example.surveyanalyze.survey.service.SurveyAnalyzeService;
import jakarta.servlet.http.HttpServletRequest;
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
//    @Cacheable(value = "/research/survey/load/{id}", key = "#id")
    @GetMapping(value = "/research/survey/load/{id}")
    public SurveyDetailDto readSurvey(@PathVariable Long id) {
        return surveyService.getSurveyDetailDto(id);
    }

    // 설문 상세 분석 조회
//    @Cacheable(value = "/research/analyze/{id}", key = "#id")
    @GetMapping(value = "/research/analyze/{id}")
    public SurveyAnalyzeDto readDetailAnalyze(@PathVariable Long id) {
        return surveyService.readSurveyDetailAnalyze(id);
    }
}
