package com.example.surveydocument.survey.controller;

import com.example.surveydocument.survey.domain.Choice;
import com.example.surveydocument.survey.domain.QuestionDocument;
import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.exception.InvalidTokenException;
import com.example.surveydocument.survey.request.DateDto;
import com.example.surveydocument.survey.request.PageRequestDto;
import com.example.surveydocument.survey.request.SurveyRequestDto;
import com.example.surveydocument.survey.request.SurveyTemplateRequestDTO;
import com.example.surveydocument.survey.response.ManagementResponseDto;
import com.example.surveydocument.survey.response.SurveyDetailDto;
import com.example.surveydocument.survey.response.WordCloudDto;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@RequestMapping("/api/document/external")
public class SurveyDocumentExternalController {

    private final SurveyDocumentService surveyService;

    //Response (id 값을 보내주기)
    @PostMapping(value = "/create")
    public Long create(HttpServletRequest request, @RequestBody SurveyRequestDto surveyForm) throws InvalidTokenException, UnknownHostException {
        return surveyService.createSurvey(request, surveyForm);
    }

    // grid 로 조회
    @PostMapping(value = "/survey-list-grid")
    public List<SurveyDocument> readListGrid(HttpServletRequest request, @RequestBody PageRequestDto pageRequest) {
        return surveyService.readSurveyListByGrid(request, pageRequest);
    }

    // list 로 조회
    @PostMapping(value = "/survey-list")
    public Page<SurveyDocument> readList(HttpServletRequest request, @RequestBody PageRequestDto pageRequest) throws Exception {
        return surveyService.readSurveyList(request, pageRequest);
    }

    @GetMapping(value = "/survey-list/{id}")
    public SurveyDetailDto readDetail(HttpServletRequest request, @PathVariable Long id) throws InvalidTokenException {
        return surveyService.readSurveyDetail(request, id);
    }

    // 설문 수정
    @PutMapping("/update/{id}")
    public void updateSurvey(HttpServletRequest request,@RequestBody SurveyRequestDto requestDto, @PathVariable Long id) {
        surveyService.updateSurvey(request, requestDto, id);
    }

    // 설문 삭제
    @PatchMapping("/delete/{id}")
    public String deleteSurvey(@PathVariable Long id) {
        surveyService.deleteSurvey(id);
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
    public void countSurveyDocument(@PathVariable Long id) throws Exception {
        surveyService.countSurveyDocument(id);
    }

    //SurveyTemplate 조회
    @GetMapping(value = "/template-load/{id}")
    public SurveyDetailDto loadTemplateSurvey(@PathVariable Long id) {
        return surveyService.getSurveyTemplateDetailDto(id);
    }

    @PostMapping(value = "/template-create")
    public String createTemlpate(HttpServletRequest request, @RequestBody SurveyTemplateRequestDTO surveyForm) throws InvalidTokenException, UnknownHostException {
        surveyService.createTemplateSurvey(request, surveyForm);

        return "Success";
    }

}
