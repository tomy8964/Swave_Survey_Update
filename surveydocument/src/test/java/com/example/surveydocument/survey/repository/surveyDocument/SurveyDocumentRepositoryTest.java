package com.example.surveydocument.survey.repository.surveyDocument;

import com.example.surveydocument.survey.domain.*;
import com.example.surveydocument.survey.response.ManagementResponseDto;
import com.example.surveydocument.survey.response.SurveyPageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.example.surveydocument.survey.service.SurveyDocumentServiceTest.createSurveyDocument;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class SurveyDocumentRepositoryTest {

    @Autowired
    SurveyDocumentRepository surveyDocumentRepository;

    @Test
    @DisplayName("설문 목록 조회 테스트 - 제목 오름차순")
    public void pagingSurvey1() {
        //given
        SurveyDocument surveyDocument = createSurveyDocument();
        surveyDocumentRepository.save(surveyDocument);
        Long userId = 1L;
        String sortWhat = "title";
        String sortHow = "ascending";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<SurveyPageDto> result = surveyDocumentRepository.pagingSurvey(userId, sortWhat, sortHow, pageable);

        // then
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("설문 목록 조회 테스트 - 제목 내림차순")
    public void pagingSurvey2() {
        //given
        SurveyDocument surveyDocument = createSurveyDocument();
        surveyDocumentRepository.save(surveyDocument);
        Long userId = 1L;
        String sortWhat = "title";
        String sortHow = "descending";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<SurveyPageDto> result = surveyDocumentRepository.pagingSurvey(userId, sortWhat, sortHow, pageable);

        // then
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("설문 목록 조회 테스트 - 날짜 오름차순")
    public void pagingSurvey3() {
        //given
        SurveyDocument surveyDocument = createSurveyDocument();
        surveyDocumentRepository.save(surveyDocument);
        Long userId = 1L;
        String sortWhat = "";
        String sortHow = "ascending";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<SurveyPageDto> result = surveyDocumentRepository.pagingSurvey(userId, sortWhat, sortHow, pageable);

        // then
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("설문 목록 조회 테스트 - 날짜 내림차순")
    public void pagingSurvey4() {
        //given
        SurveyDocument surveyDocument = createSurveyDocument();
        surveyDocumentRepository.save(surveyDocument);
        Long userId = 1L;
        String sortWhat = "";
        String sortHow = "descending";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<SurveyPageDto> result = surveyDocumentRepository.pagingSurvey(userId, sortWhat, sortHow, pageable);

        // then
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("설문 조회 테스트")
    public void findSurveyById() {
        //given
        SurveyDocument surveyDocument = createSurveyDocument();
        SurveyDocument saveSurvey = surveyDocumentRepository.save(surveyDocument);

        // when
        SurveyDocument findSurvey = surveyDocumentRepository.findSurveyById(saveSurvey.getId()).get();

        // then
        assertEquals(saveSurvey.getId(), findSurvey.getId());
        assertEquals(surveyDocument.getTitle(), findSurvey.getTitle());
        assertEquals(surveyDocument.getDescription(), findSurvey.getDescription());
        assertEquals(surveyDocument.getReliability(), findSurvey.getReliability());
        assertEquals(surveyDocument.getCountAnswer(), findSurvey.getCountAnswer());

        Design design = surveyDocument.getDesign();
        assertEquals(design.getFont(), findSurvey.getDesign().getFont());
        assertEquals(design.getFontSize(), findSurvey.getDesign().getFontSize());
        assertEquals(design.getBackColor(), findSurvey.getDesign().getBackColor());

        DateManagement dateManagement = surveyDocument.getDate();
        assertEquals(dateManagement.getStartDate(), findSurvey.getDate().getStartDate());
        assertEquals(dateManagement.getDeadline(), findSurvey.getDate().getDeadline());
        assertEquals(dateManagement.getIsEnabled(), findSurvey.getDate().getIsEnabled());

        List<QuestionDocument> questionDocuments = surveyDocument.getQuestionDocumentList();
        List<QuestionDocument> questionDocumentList = findSurvey.getQuestionDocumentList();
        assertEquals(questionDocuments.size(), questionDocumentList.size());
        for (int i = 0; i < questionDocuments.size(); i++) {
            assertEquals(questionDocuments.get(i).getTitle(), questionDocumentList.get(i).getTitle());
            assertEquals(questionDocuments.get(i).getQuestionType(), questionDocumentList.get(i).getQuestionType());

            List<Choice> choices = questionDocuments.get(i).getChoiceList();
            List<Choice> choiceList = questionDocumentList.get(i).getChoiceList();
            for (int j = 0; j < choices.size(); j++) {
                assertEquals(choices.get(j).getTitle(), choiceList.get(j).getTitle());
            }
        }
    }

    @Test
    @DisplayName("설문 관리 조회 테스트")
    public void findManageById() {
        //given
        SurveyDocument surveyDocument = createSurveyDocument();
        SurveyDocument saveSurvey = surveyDocumentRepository.save(surveyDocument);

        // when
        ManagementResponseDto managementResponseDto = surveyDocumentRepository.findManageById(saveSurvey.getId()).get();

        // then
        DateManagement date = saveSurvey.getDate();
        assertEquals(date.getStartDate(), managementResponseDto.getStartDate());
        assertEquals(date.getDeadline(), managementResponseDto.getEndDate());
        assertEquals(date.getIsEnabled(), managementResponseDto.getEnable());
    }

    @Test
    @DisplayName("설문 수정을 위한 조회 테스트")
    public void findByIdToUpdate() {
        //given
        SurveyDocument surveyDocument = createSurveyDocument();
        SurveyDocument saveSurvey = surveyDocumentRepository.save(surveyDocument);

        // when
        SurveyDocument findSurvey = surveyDocumentRepository.findByIdToUpdate(saveSurvey.getId()).get();

        // then
        assertEquals(saveSurvey.getId(), findSurvey.getId());
        assertEquals(surveyDocument.getTitle(), findSurvey.getTitle());
        assertEquals(surveyDocument.getDescription(), findSurvey.getDescription());
        assertEquals(surveyDocument.getReliability(), findSurvey.getReliability());
        assertEquals(surveyDocument.getCountAnswer(), findSurvey.getCountAnswer());

        Design design = surveyDocument.getDesign();
        assertEquals(design.getFont(), findSurvey.getDesign().getFont());
        assertEquals(design.getFontSize(), findSurvey.getDesign().getFontSize());
        assertEquals(design.getBackColor(), findSurvey.getDesign().getBackColor());

        DateManagement dateManagement = surveyDocument.getDate();
        assertEquals(dateManagement.getStartDate(), findSurvey.getDate().getStartDate());
        assertEquals(dateManagement.getDeadline(), findSurvey.getDate().getDeadline());
        assertEquals(dateManagement.getIsEnabled(), findSurvey.getDate().getIsEnabled());

        List<QuestionDocument> questionDocuments = surveyDocument.getQuestionDocumentList();
        List<QuestionDocument> questionDocumentList = findSurvey.getQuestionDocumentList();
        assertEquals(questionDocuments.size(), questionDocumentList.size());
        for (int i = 0; i < questionDocuments.size(); i++) {
            assertEquals(questionDocuments.get(i).getTitle(), questionDocumentList.get(i).getTitle());
            assertEquals(questionDocuments.get(i).getQuestionType(), questionDocumentList.get(i).getQuestionType());

            List<Choice> choices = questionDocuments.get(i).getChoiceList();
            List<Choice> choiceList = questionDocumentList.get(i).getChoiceList();
            for (int j = 0; j < choices.size(); j++) {
                assertEquals(choices.get(j).getTitle(), choiceList.get(j).getTitle());
            }
        }
    }

    @Test
    @DisplayName("설문 수정 테스트")
    public void updateManage() {
        //given
        SurveyDocument surveyDocument = createSurveyDocument();
        SurveyDocument saveSurvey = surveyDocumentRepository.save(surveyDocument);

        // when
        Boolean changeBoolean = surveyDocumentRepository.updateManage(saveSurvey.getId(), false);

        // then
        assertEquals(saveSurvey.getDate().getIsEnabled(), !changeBoolean);
    }
}