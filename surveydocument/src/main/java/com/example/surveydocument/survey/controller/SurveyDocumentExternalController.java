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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@RequestMapping("/api/document/external")
public class SurveyDocumentExternalController {

    private final SurveyDocumentService surveyService;

    //Response (저장된 survey document id 값을 보내주기)
    @PostMapping(value = "/create")
    public ResponseEntity<Long> create(HttpServletRequest request, @RequestBody SurveyRequestDto surveyForm) {
        return new ResponseEntity<>(surveyService.createSurvey(request, surveyForm), HttpStatus.CREATED);
    }

    @PostMapping(value = "/survey-list")
    @Cacheable(value = "surveyPage", key = "'surveyPage-' + #pageRequest", cacheManager = "cacheManager")
    public ResponseEntity<Page<SurveyPageDto>> readList(HttpServletRequest request, @RequestBody PageRequestDto pageRequest) {
        return new ResponseEntity<>(surveyService.readSurveyList(request, pageRequest), HttpStatus.OK);
    }

    @GetMapping(value = "/survey-list/{id}")
    @Cacheable(value = "survey", key = "'survey-' + #id", cacheManager = "cacheManager")
    public ResponseEntity<SurveyDetailDto> readDetail(@PathVariable Long id) {
        return new ResponseEntity<>(surveyService.readSurveyDetail(id), HttpStatus.OK);
    }

    // 설문 수정
    @PutMapping("/update/{id}")
    @Caching(evict = {
            @CacheEvict(value = "surveyPage", allEntries = true, cacheManager = "cacheManager"),
            @CacheEvict(value = "survey", key = "'survey-' + #id", cacheManager = "cacheManager"),
            @CacheEvict(value = "survey2", key = "'survey2-' + #id", cacheManager = "cacheManager")
    })
    public ResponseEntity<Long> updateSurvey(HttpServletRequest request, @RequestBody SurveyRequestDto requestDto, @PathVariable Long id) {
        return new ResponseEntity<>(surveyService.updateSurvey(request, requestDto, id), HttpStatus.NO_CONTENT);
    }

    // 설문 삭제
    @PatchMapping("/delete/{id}")
    @Caching(evict = {
            @CacheEvict(value = "surveyPage", allEntries = true, cacheManager = "cacheManager"),
            @CacheEvict(value = "survey", key = "'survey-' + #id", cacheManager = "cacheManager"),
            @CacheEvict(value = "survey2", key = "'survey2-' + #id", cacheManager = "cacheManager")
    })
    public ResponseEntity<Long> deleteSurvey(HttpServletRequest request, @PathVariable Long id) {
        return new ResponseEntity<>(surveyService.deleteSurvey(request, id), HttpStatus.NO_CONTENT);
    }

    // 설문 관리 날짜
    @PatchMapping("/management/date/{id}")
    public ResponseEntity<Long> managementDate(@PathVariable Long id, @RequestBody DateDto dateRequest) {
        return new ResponseEntity<>(surveyService.managementDate(id, dateRequest), HttpStatus.NO_CONTENT);
    }

    // 설문 관리 Get
    @GetMapping("/management/{id}")
    public ResponseEntity<ManagementResponseDto> managementSurvey(@PathVariable Long id) {
        return new ResponseEntity<>(surveyService.managementSurvey(id), HttpStatus.OK);
    }

    // 설문 활성화/비활성화
    @PatchMapping("/management/enable/{id}")
    public ResponseEntity<Boolean> managementEnable(@PathVariable Long id, @RequestBody Boolean enable) {
        return new ResponseEntity<>(surveyService.managementEnable(id, enable), HttpStatus.NO_CONTENT);
    }
}
