package com.example.surveyanswer.survey;

import com.example.surveyanswer.survey.domain.*;
import com.example.surveyanswer.survey.repository.questionAnswer.QuestionAnswerRepository;
import com.example.surveyanswer.survey.repository.surveyAnswer.SurveyAnswerRepository;
import com.example.surveyanswer.survey.response.*;
import com.example.surveyanswer.survey.restAPI.service.RestAPIService;
import com.example.surveyanswer.survey.service.SurveyAnswerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
public class AnswerServiceTest {

//    @Autowired
    private SurveyAnswerService surveyAnswerService;
    @Autowired
    private SurveyAnswerRepository surveyAnswerRepository;
    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;
    @Autowired
    private RestAPIService restAPIService;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        // Create Survey Document
        ObjectMapper objectMapper = new ObjectMapper();

        SurveyDocument surveyDocument = createSurveyDocument();
        String survey = objectMapper.writeValueAsString(surveyDocument);
        // Start the MockWebServer
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Configure the MockWebServer's behavior
        // Configure the Dispatcher
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                MockResponse response = new MockResponse();

                // Customize the response based on the request URL
                if (path.startsWith("/api/document/internal/count/")) {
                    response.setResponseCode(200)
                            .setBody("endpoint count 확인");
                } else if (path.startsWith("/api/document/internal/getSurveyDocument/")) {
                    System.out.println("getSurveyDocument");
                    response.setResponseCode(200)
                            .setBody(survey)
                            .addHeader("Content-Type", "application/json");
                } else {
                    response.setResponseCode(200);
                }

                return response;
            }
        });

        // Get the base URL of the mock server
        String url = mockWebServer.url("/").toString();
        URI uri = URI.create(url);
        String baseUrl = uri.getHost() + ":" + uri.getPort();

        System.out.println(baseUrl);


        // Initialize other dependencies and the service under test
        MockitoAnnotations.openMocks(this);
//        surveyAnswerRepository = Mockito.mock(SurveyAnswerRepository.class);
//        questionAnswerRepository = Mockito.mock(QuestionAnswerRepository.class);
//        restAPIService = Mockito.mock(RestAPIService.class);
        surveyAnswerService = new SurveyAnswerService(surveyAnswerRepository, questionAnswerRepository, restAPIService);

        // Update the base URL of the service to use the mock server
        restAPIService.setGateway(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Shutdown the MockWebServer
        mockWebServer.shutdown();
    }

    @Test
    @Transactional
    @DisplayName("Survey Answer Create Test")
    void createAnswerTest() {
        //given
        QuestionResponseDto questionResponseDto1 = QuestionResponseDto.builder()
                .title("Question 1")
                .answer("Answer 1")
                .answerId(1L)
                .type(2)
                .build();
        QuestionResponseDto questionResponseDto2 = QuestionResponseDto.builder()
                .title("Question 2")
                .answer("Answer 2")
                .type(2)
                .answerId(1L)
                .build();

        List<QuestionResponseDto> questionResponseDtoList = new ArrayList<>();
        questionResponseDtoList.add(questionResponseDto1);
        questionResponseDtoList.add(questionResponseDto2);

        SurveyResponseDto surveyResponseDto = SurveyResponseDto.builder()
                .title("설문 제목")
                .description("설문 설명")
                .reliability(false)
                .questionResponse(new ArrayList<>())
                .id(1L)
                .build();

        surveyResponseDto.setQuestionResponse(questionResponseDtoList);

        //when
        surveyAnswerService.createSurveyAnswer(surveyResponseDto);

        //then
        List<SurveyAnswer> surveyAnswersBySurveyDocumentId = surveyAnswerRepository.findSurveyAnswersBySurveyDocumentId(1L);
        for (SurveyAnswer surveyAnswer : surveyAnswersBySurveyDocumentId) {
            assertEquals(surveyAnswer.getSurveyDocumentId(), 1L);
            assertEquals(surveyAnswer.getTitle(), "설문 제목");
            assertEquals(surveyAnswer.getDescription(), "설문 설명");
            assertEquals(surveyAnswer.getType(), 0);
            for (QuestionAnswer questionAnswer : surveyAnswer.getQuestionanswersList()) {
                assertEquals(questionAnswer.getSurveyDocumentId(), 1L);
                assertEquals(questionAnswer.getSurveyAnswerId().getId(), surveyAnswer.getId());
            }
        }
    }

    @Test
    @DisplayName("Survey Answer Create Reliability Test")
    void createAnswerReliabilityTest() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("Participate Survey Answer Test")
    void participateSurvey() throws JsonProcessingException {
        //given
        //when
        SurveyDetailDto participantDetailDto = surveyAnswerService.getParticipantSurvey(1L);

        //then
        assertEquals(participantDetailDto.getId(), 1L);
        assertEquals(participantDetailDto.getTitle(), "설문 제목");
        assertEquals(participantDetailDto.getDescription(), "설문 설명");
        assertEquals(participantDetailDto.getReliability(), false);

        QuestionDetailDto questionDetailDto1 = participantDetailDto.getQuestionList().get(0);
        assertEquals(questionDetailDto1.getTitle(), "Question 1");
        assertEquals(questionDetailDto1.getQuestionType(), 2);
        ChoiceDetailDto choiceDetailDto1 = questionDetailDto1.getChoiceList().get(0);
        assertEquals(choiceDetailDto1.getTitle(), "Choice 1");
        ChoiceDetailDto choiceDetailDto2 = questionDetailDto1.getChoiceList().get(1);
        assertEquals(choiceDetailDto2.getTitle(), "Choice 2");

        QuestionDetailDto questionDetailDto2 = participantDetailDto.getQuestionList().get(1);
        assertEquals(questionDetailDto2.getTitle(), "Question 2");
        assertEquals(questionDetailDto2.getQuestionType(), 2);
        ChoiceDetailDto choiceDetailDto3 = questionDetailDto2.getChoiceList().get(0);
        assertEquals(choiceDetailDto3.getTitle(), "Choice 3");
        ChoiceDetailDto choiceDetailDto4 = questionDetailDto2.getChoiceList().get(1);
        assertEquals(choiceDetailDto4.getTitle(), "Choice 4");

    }

    private static SurveyDocument createSurveyDocument() {
        SurveyDocument surveyDocument = SurveyDocument.builder()
                .title("설문 제목")
                .description("설문 설명")
                .reliability(false)
                .questionDocumentList(new ArrayList<>())
                .build();

        List<QuestionDocument> questionDocumentList = new ArrayList<>();

        QuestionDocument questionDocument1 = QuestionDocument.builder()
                .surveyDocument(surveyDocument)
                .questionType(2)
                .title("Question 1")
                .choiceList(new ArrayList<>())
                .build();

        List<Choice> choiceList1 = new ArrayList<>();

        Choice choice1 = Choice.builder()
                .title("Choice 1")
                .question_id(questionDocument1)
                .build();
        choiceList1.add(choice1);

        Choice choice2 = Choice.builder()
                .title("Choice 2")
                .question_id(questionDocument1)
                .build();
        choiceList1.add(choice2);

        questionDocument1.setChoiceList(choiceList1);

        QuestionDocument questionDocument2 = QuestionDocument.builder()
                .surveyDocument(surveyDocument)
                .questionType(2)
                .title("Question 2")
                .choiceList(new ArrayList<>())
                .build();

        List<Choice> choiceList2 = new ArrayList<>();

        Choice choice3 = Choice.builder()
                .title("Choice 3")
                .question_id(questionDocument1)
                .build();
        choiceList2.add(choice3);

        Choice choice4 = Choice.builder()
                .title("Choice 4")
                .question_id(questionDocument1)
                .build();
        choiceList2.add(choice4);

        questionDocument2.setChoiceList(choiceList2);

        questionDocumentList.add(questionDocument1);
        questionDocumentList.add(questionDocument2);

        surveyDocument.setQuestionDocumentList(questionDocumentList);
        surveyDocument.setId(1L);

        return surveyDocument;
    }
}
