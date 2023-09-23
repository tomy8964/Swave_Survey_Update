package com.example.surveydocument.survey.repository.survey;

import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.request.PageRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SurveyRepositoryCustom {
    Page<SurveyDocument> surveyDocumentPaging(Long userCode, Pageable pageable);
    SurveyDocument surveyDocumentDetail(Long userCode, SurveyDocument surveyDocumentRequest);
    void surveyDocumentCount(SurveyDocument surveyDocument);
    List<SurveyDocument> getSurveyDocumentListGrid(Long userCode, PageRequestDto pageRequest);

}