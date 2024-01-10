package com.example.surveydocument.survey.controller;

import com.example.surveydocument.survey.request.DateDto;
import com.example.surveydocument.survey.request.PageRequestDto;
import com.example.surveydocument.survey.request.SurveyRequestDto;
import com.example.surveydocument.survey.response.ManagementResponseDto;
import com.example.surveydocument.survey.response.SurveyDetailDto;
import com.example.surveydocument.survey.response.SurveyPageDto;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@RequestMapping("/api/document/external")
public class SurveyDocumentExternalController {

    private final SurveyDocumentService surveyService;

    //Response (저장된 survey document id 값을 보내주기)
    @PostMapping(value = "/create")
    public Long create(HttpServletRequest request, @RequestBody SurveyRequestDto surveyForm) {
        return surveyService.createSurvey(request, surveyForm);
    }

    @PostMapping(value = "/survey-list")
    @Cacheable(value = "surveyPage", key = "'surveyPage-' + #pageRequest", cacheManager = "cacheManager")
    public Page<SurveyPageDto> readList(HttpServletRequest request, @RequestBody PageRequestDto pageRequest) {
        return surveyService.readSurveyList(request, pageRequest);
    }

    @GetMapping(value = "/survey-list/{id}")
    @Cacheable(value = "survey", key = "'survey-' + #id", cacheManager = "cacheManager")
    public SurveyDetailDto readDetail(@PathVariable Long id) {
        return surveyService.readSurveyDetail(id);
    }

    // 설문 수정
    @PutMapping("/update/{id}")
    @Caching(evict = {
            @CacheEvict(value = "surveyPage", allEntries = true, cacheManager = "cacheManager"),
            @CacheEvict(value = "survey", key = "'survey-' + #id", cacheManager = "cacheManager"),
            @CacheEvict(value = "survey2", key = "'survey2-' + #id", cacheManager = "cacheManager")
    })
    public void updateSurvey(HttpServletRequest request, @RequestBody SurveyRequestDto requestDto, @PathVariable Long id) {
        surveyService.updateSurvey(request, requestDto, id);
    }

    // 설문 삭제
    @PatchMapping("/delete/{id}")
    @Caching(evict = {
            @CacheEvict(value = "surveyPage", allEntries = true, cacheManager = "cacheManager"),
            @CacheEvict(value = "survey", key = "'survey-' + #id", cacheManager = "cacheManager"),
            @CacheEvict(value = "survey2", key = "'survey2-' + #id", cacheManager = "cacheManager")
    })
    public String deleteSurvey(HttpServletRequest request, @PathVariable Long id) {
        surveyService.deleteSurvey(request, id);
        return "Success";
    }

    // 설문 관리 날짜
    @PatchMapping("/management/date/{id}")
    public void managementDate(@PathVariable Long id, @RequestBody DateDto dateRequest) {
        surveyService.managementDate(id, dateRequest);
    }

    // 설문 관리 응답 여부
    @PatchMapping("/management/enable/{id}")
    public void managementEnable(@PathVariable Long id, @RequestBody Boolean enable) {
        surveyService.managementEnable(id, enable);
    }

    // 설문 관리 Get
    @GetMapping("/management/{id}")
    public ManagementResponseDto managementSurvey(@PathVariable Long id) {
        return surveyService.managementSurvey(id);
    }

    // 설문 응답수 추가
    @GetMapping("/survey/count/{id}")
    public void countSurveyDocument(@PathVariable Long id) {
        surveyService.countSurveyDocument(id);
    }

}
