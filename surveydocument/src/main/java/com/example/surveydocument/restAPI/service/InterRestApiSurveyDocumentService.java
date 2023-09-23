package com.example.surveydocument.restAPI.service;

import com.example.surveydocument.survey.domain.Survey;
import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.exception.InvalidTokenException;
import com.example.surveydocument.survey.repository.survey.SurveyRepository;
import com.example.surveydocument.survey.repository.surveyDocument.SurveyDocumentRepository;
import com.example.surveydocument.survey.response.SurveyDetailDto;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableTransactionManagement
public class InterRestApiSurveyDocumentService {
    private final SurveyRepository surveyRepository;
    private final SurveyDocumentRepository surveyDocumentRepository;
    private final SurveyDocumentService surveyDocumentService;

    // 유저가 생성되면서 자동으로 Survey 도 생성한다
    public void saveUserInSurvey(Long userCode) {

        Survey survey = Survey.builder()
                .surveyDocumentList(new ArrayList<>())
                .userCode(userCode)
                .build();
        surveyRepository.save(survey);
    }

}
