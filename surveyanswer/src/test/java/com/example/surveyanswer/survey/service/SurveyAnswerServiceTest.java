package com.example.surveyanswer.survey.service;

import com.example.surveyanswer.survey.domain.QuestionAnswer;
import com.example.surveyanswer.survey.domain.SurveyAnswer;
import com.example.surveyanswer.survey.repository.questionAnswer.QuestionAnswerRepository;
import com.example.surveyanswer.survey.repository.surveyAnswer.SurveyAnswerRepository;
import com.example.surveyanswer.survey.response.*;
import com.example.surveyanswer.survey.restAPI.service.RestAPIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@Transactional
@SpringBootTest
public class SurveyAnswerServiceTest {

    //    @Autowired
    private SurveyAnswerService surveyAnswerService;
    @Autowired
    private SurveyAnswerRepository surveyAnswerRepository;
    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;
    @Autowired
    private RestAPIService restAPIService;

    private MockWebServer mockWebServer;

    private static SurveyDetailDto createSurveyDetailDto() {
        SurveyDetailDto surveyDetailDto = SurveyDetailDto.builder()
                .id(1L)
                .title("설문 제목")
                .description("설문 설명")
                .reliability(false)
                .countAnswer(100)
                .design(new DesignResponseDto("font", 10, "backgroundColor"))
                .enable(true)
                .startDate(new Date())
                .endDate(new Date())
                .build();

        List<QuestionDetailDto> questionDetailDtoList = new ArrayList<>();

        QuestionDetailDto questionDetailDto1 = QuestionDetailDto.builder()
                .id(1L)
                .title("Question 1")
                .questionType(2)
                .build();

        List<ChoiceDetailDto> choiceDetailDtoList1 = new ArrayList<>();

        ChoiceDetailDto choice1 = ChoiceDetailDto.builder()
                .id(1L)
                .title("Choice 1")
                .count(0)
                .build();
        choiceDetailDtoList1.add(choice1);

        ChoiceDetailDto choice2 = ChoiceDetailDto.builder()
                .id(2L)
                .title("Choice 2")
                .count(2)
                .build();
        choiceDetailDtoList1.add(choice2);

        questionDetailDto1.setChoiceList(choiceDetailDtoList1);

        QuestionDetailDto questionDetailDto2 = QuestionDetailDto.builder()
                .id(2L)
                .questionType(2)
                .title("Question 2")
                .build();

        List<ChoiceDetailDto> choiceDetailDtoList2 = new ArrayList<>();

        ChoiceDetailDto choice3 = ChoiceDetailDto.builder()
                .id(3L)
                .title("Choice 3")
                .count(1)
                .build();
        choiceDetailDtoList2.add(choice3);

        ChoiceDetailDto choice4 = ChoiceDetailDto.builder()
                .id(4L)
                .title("Choice 4")
                .count(1)
                .build();

        choiceDetailDtoList2.add(choice4);

        questionDetailDto2.setChoiceList(choiceDetailDtoList2);

        questionDetailDtoList.add(questionDetailDto1);
        questionDetailDtoList.add(questionDetailDto2);

        surveyDetailDto.setQuestionList(questionDetailDtoList);

        return surveyDetailDto;
    }

    @BeforeEach
    void setUp() throws IOException {
        // Create Survey Document
        ObjectMapper objectMapper = new ObjectMapper();

        SurveyDetailDto surveyDetailDto = createSurveyDetailDto();
        String survey1 = objectMapper.writeValueAsString(surveyDetailDto);

        surveyDetailDto.setReliability(true);
        String surveyReliability = objectMapper.writeValueAsString(surveyDetailDto);
        // Start the MockWebServer
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Configure the MockWebServer's behavior
        // Configure the Dispatcher
        mockWebServer.setDispatcher(new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                MockResponse response = new MockResponse();

                // Customize the response based on the request URL
                if (path.startsWith("/api/document/internal/count/")) {
                    response.setResponseCode(200)
                            .setBody("endpoint count 확인");
                } else if (path.startsWith("/api/document/internal/getSurveyDocumentToAnswer/1")) {
                    System.out.println("getSurveyDocumentToAnswer");
                    response.setResponseCode(200)
                            .setBody(survey1)
                            .addHeader("Content-Type", "application/json");
                } else if (path.startsWith("/api/document/internal/getSurveyDocumentToAnswer/2")) {
                    System.out.println("getSurveyDocumentToAnswer");
                    response.setResponseCode(200)
                            .setBody(surveyReliability)
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
    @DisplayName("응답 저장")
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
                .questionResponse(questionResponseDtoList)
                .id(1L)
                .build();

        //when
        surveyAnswerService.createSurveyAnswer(surveyResponseDto);

        //then
        List<SurveyAnswer> surveyAnswersBySurveyDocumentId = surveyAnswerRepository.findSurveyAnswersBySurveyDocumentId(1L);
        for (SurveyAnswer surveyAnswer : surveyAnswersBySurveyDocumentId) {
            assertEquals(surveyAnswer.getSurveyDocumentId(), 1L);
            assertEquals(surveyAnswer.getTitle(), "설문 제목");
            assertEquals(surveyAnswer.getDescription(), "설문 설명");
            assertEquals(surveyAnswer.getType(), 0);
            for (QuestionAnswer questionAnswer : surveyAnswer.getQuestionAnswersList()) {
                assertEquals(questionAnswer.getSurveyDocumentId(), 1L);
                assertEquals(questionAnswer.getSurveyAnswer().getId(), surveyAnswer.getId());
            }
        }
    }


    @Test
    @DisplayName("진정성 검사 성공 응답 저장 O")
    void createAnswerReliabilityTest1() {
        //given
        QuestionResponseDto questionResponseDto = QuestionResponseDto.builder()
                .title("이 문항에는 매우 부정적이다를 선택해주세요.")
                .answer("매우 부정적이다")
                .type(2)
                .answerId(-1L)
                .build();
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
        questionResponseDtoList.add(questionResponseDto);
        questionResponseDtoList.add(questionResponseDto1);
        questionResponseDtoList.add(questionResponseDto2);

        SurveyResponseDto surveyResponseDto = SurveyResponseDto.builder()
                .title("설문 제목")
                .description("설문 설명")
                .reliability(true)
                .questionResponse(questionResponseDtoList)
                .id(1L)
                .build();

        //when
        surveyAnswerService.createSurveyAnswer(surveyResponseDto);

        //then
        List<SurveyAnswer> surveyAnswersBySurveyDocumentId = surveyAnswerRepository.findSurveyAnswersBySurveyDocumentId(1L);
        for (SurveyAnswer surveyAnswer : surveyAnswersBySurveyDocumentId) {
            assertEquals(surveyAnswer.getSurveyDocumentId(), 1L);
            assertEquals(surveyAnswer.getTitle(), "설문 제목");
            assertEquals(surveyAnswer.getDescription(), "설문 설명");
            assertEquals(surveyAnswer.getType(), 0);
            for (QuestionAnswer questionAnswer : surveyAnswer.getQuestionAnswersList()) {
                assertEquals(questionAnswer.getSurveyDocumentId(), 1L);
                assertEquals(questionAnswer.getSurveyAnswer().getId(), surveyAnswer.getId());
            }
        }
    }

    @Test
    @DisplayName("진정성 검사 실패 응답 저장 X")
    void createAnswerReliabilityTest2() {
        //given
        QuestionResponseDto questionResponseDto = QuestionResponseDto.builder()
                .title("이 문항에는 매우 부정적이다를 선택해주세요.")
                .answer("진정성 검사 실패 답변")
                .type(2)
                .answerId(-1L)
                .build();

        List<QuestionResponseDto> questionResponseDtoList = new ArrayList<>();
        questionResponseDtoList.add(questionResponseDto);

        SurveyResponseDto surveyResponseDto = SurveyResponseDto.builder()
                .title("설문 제목")
                .description("설문 설명")
                .reliability(true)
                .questionResponse(questionResponseDtoList)
                .id(1L)
                .build();

        //when
        assertThrows(RuntimeException.class, () -> surveyAnswerService.createSurveyAnswer(surveyResponseDto));
    }

    @Test
    @DisplayName("설문 참여")
    void participateSurvey1() {
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

    @Test
    @DisplayName("설문 참여 진정성 검사 O")
    void participateSurvey2() {
        //when
        SurveyDetailDto participantDetailDto = surveyAnswerService.getParticipantSurvey(2L);

        //then
        assertEquals(participantDetailDto.getId(), 1L);
        assertEquals(participantDetailDto.getTitle(), "설문 제목");
        assertEquals(participantDetailDto.getDescription(), "설문 설명");
        assertEquals(participantDetailDto.getReliability(), true);
        assertEquals(participantDetailDto.getQuestionList().size(), 3);
    }
    
//    @Test
//    @DisplayName("question id로 question answer 찾기")
//    public void getQuestionAnswers() {
//        //given
//        QuestionResponseDto questionResponseDto1 = QuestionResponseDto.builder()
//                .title("Question 1")
//                .answer("Answer 1")
//                .answerId(1L)
//                .type(2)
//                .build();
//        QuestionResponseDto questionResponseDto2 = QuestionResponseDto.builder()
//                .title("Question 2")
//                .answer("Answer 2")
//                .type(2)
//                .answerId(1L)
//                .build();
//
//        List<QuestionResponseDto> questionResponseDtoList = new ArrayList<>();
//        questionResponseDtoList.add(questionResponseDto1);
//        questionResponseDtoList.add(questionResponseDto2);
//
//        SurveyResponseDto surveyResponseDto = SurveyResponseDto.builder()
//                .title("설문 제목")
//                .description("설문 설명")
//                .reliability(false)
//                .questionResponse(questionResponseDtoList)
//                .id(1L)
//                .build();
//        surveyAnswerService.createSurveyAnswer(surveyResponseDto);
//
//        //when
//        List<QuestionAnswer> findQuestionAnswerList = surveyAnswerService.getQuestionAnswers(1L);
//        QuestionAnswer questionAnswer1 = findQuestionAnswerList.get(0);
//        QuestionAnswer questionAnswer2 = findQuestionAnswerList.get(1);
//
//        //then
//        assertEquals(questionAnswer1.getCheckAnswer(), "Answer 1");
//        assertEquals(questionAnswer2.getCheckAnswer(), "Answer 2");
//    }
    
}