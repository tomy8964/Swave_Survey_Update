package com.example.surveydocument.survey.repository.surveyDocument;

import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.response.ManagementResponseDto;
import com.example.surveydocument.survey.response.SurveyPageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface SurveyDocumentRepositoryCustom {
    Page<SurveyPageDto> pagingSurvey(Long userId, String sortWhat, String sortHow, Pageable pageable);

    Optional<SurveyDocument> findSurveyById(Long surveyDocumentId);
    ManagementResponseDto findManageById(Long surveyDocumentId);

    Optional<SurveyDocument> findByIdToUpdate(Long surveyId);

    void updateManage(Long id, Boolean enable);
}
