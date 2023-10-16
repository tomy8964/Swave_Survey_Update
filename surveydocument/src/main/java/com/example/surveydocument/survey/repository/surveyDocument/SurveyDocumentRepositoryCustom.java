package com.example.surveydocument.survey.repository.surveyDocument;

import com.example.surveydocument.survey.response.SurveyPageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SurveyDocumentRepositoryCustom {



    Page<SurveyPageDto> pagingSurvey(Long userId, String sortWhat, String sortHow, Pageable pageable);
}
