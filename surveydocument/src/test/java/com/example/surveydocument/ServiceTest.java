package com.example.surveydocument;

import com.example.surveydocument.survey.domain.Design;
import com.example.surveydocument.survey.domain.QuestionDocument;
import com.example.surveydocument.survey.domain.Survey;
import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.repository.questionDocument.QuestionDocumentRepository;
import com.example.surveydocument.survey.repository.survey.SurveyRepository;
import com.example.surveydocument.survey.repository.surveyDocument.SurveyDocumentRepository;
import com.example.surveydocument.survey.request.DateDto;
import com.example.surveydocument.survey.request.DesignRequestDto;
import com.example.surveydocument.survey.request.QuestionRequestDto;
import com.example.surveydocument.survey.request.SurveyRequestDto;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import jakarta.transaction.Transactional;
import okhttp3.mockwebserver.MockWebServer;
import org.hibernate.annotations.CreationTimestamp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.TestInstance.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class ServiceTest {
    @Autowired
    SurveyDocumentService documentService;
    @Autowired
    SurveyDocumentRepository documentRepository;
    @Autowired
    QuestionDocumentRepository questionDocumentRepository;
    @Autowired
    SurveyRepository surveyRepository;

//    @BeforeAll
//    @TestInstance(Lifecycle.PER_CLASS)
//    void clean() {
//        documentRepository.deleteAll();
//    }

    @Test @DisplayName("설문 저장")
    @Order(1)
    void service_test_1() {
        // given
        DesignRequestDto design = DesignRequestDto.builder()
                .font("폰트")
                .fontSize(1)
                .backColor("검은색").build();

        QuestionRequestDto questionRequest = QuestionRequestDto.builder()
                .title("설문 문항")
                .build();

        List<QuestionRequestDto> questionRequestList = new ArrayList<>();
        questionRequestList.add(questionRequest);

        SurveyRequestDto surveyRequest = SurveyRequestDto.builder()
                .title("설문 제목")
                .description("설문 내용")
                .startDate(new Date())
                .endDate(new Date())
                .questionRequest(questionRequestList)
                .design(design)
                .build();

        Survey survey = Survey.builder()
                .surveyDocumentList(new ArrayList<>())
                .build();

        surveyRepository.save(survey);

        // when
        documentService.createTest(survey, surveyRequest);

        // then
        SurveyDocument surveyDocument = documentRepository.findAll().get(0);

        assertThat(surveyRequest.getTitle()).isEqualTo(surveyDocument.getTitle());
        assertThat(surveyRequest.getDesign().getFont()).isEqualTo(surveyDocument.getDesign().getFont());
        assertThat(surveyRequest.getStartDate()).isEqualTo(surveyDocument.getDate().getStartDate());
        assertThat(questionRequest.getTitle()).isEqualTo(surveyDocument.getQuestionDocumentList().get(0).getTitle());

    }
    @Test @DisplayName("설문 수정") @Transactional
//    @Order(2)
    void service_test_2() {
        // given
        SurveyDocument surveyDocument = documentRepository.findAll().get(0);

        QuestionRequestDto questionRequest1= QuestionRequestDto.builder()
                .title("설문 문항 수정")
                .build();
        QuestionRequestDto questionRequest2= QuestionRequestDto.builder()
                .title("설문 문항 추가")
                .build();

        List<QuestionRequestDto> questionRequestList = new ArrayList<>();
        questionRequestList.add(questionRequest1);
        questionRequestList.add(questionRequest2);

        SurveyRequestDto surveyRequest = SurveyRequestDto.builder()
                .title("설문 제목 수정")
                .description("설문 내용 수정")
                .startDate(new Date())
                .endDate(new Date())
                .questionRequest(questionRequestList)
                .build();

        // when
        documentService.updateTest(surveyDocument, surveyRequest);

        // then
        SurveyDocument surveyDocumentAfter = documentRepository.findAll().get(0);

        assertThat(surveyRequest.getTitle()).isEqualTo(surveyDocumentAfter.getTitle());
    }

    @Test @DisplayName("설문 삭제")
//    @Order(2)
    void service_test3() {
        // given
        SurveyDocument surveyDocument = documentRepository.findAll().get(0);

        // when
        documentService.deleteSurvey(surveyDocument.getId());

        // then
        System.out.println(documentRepository.findById(surveyDocument.getId()).get().isDeleted());
    }

    @Test @DisplayName("날짜 수정")
    @Order(2)
    void service_test4() {
        // given
        SurveyDocument surveyDocument = documentRepository.findAll().get(0);

        DateDto dateRequest = DateDto.builder()
                .startDate(new Date())
                .endDate(new Date())
                .build();
        // when
        documentService.managementDate(surveyDocument.getId(), dateRequest);

        // then
        assertThat(surveyDocument.getDate().getStartDate()).isEqualTo(dateRequest.getStartDate());
    }
}
