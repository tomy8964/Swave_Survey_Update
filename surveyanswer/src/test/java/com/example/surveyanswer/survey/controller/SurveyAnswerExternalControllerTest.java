package com.example.surveyanswer.survey.controller;

import com.example.surveyanswer.survey.repository.questionAnswer.QuestionAnswerRepository;
import com.example.surveyanswer.survey.repository.surveyAnswer.SurveyAnswerRepository;
import com.example.surveyanswer.survey.response.*;
import com.example.surveyanswer.survey.restAPI.service.RestAPIService;
import com.example.surveyanswer.survey.service.SurveyAnswerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class SurveyAnswerExternalControllerTest {

    @Autowired
    /*
      웹 API 테스트할 때 사용
      스프링 MVC 테스트의 시작점
      HTTP GET,POST 등에 대해 API 테스트 가능
      */
    MockMvc mockMvc;

    @Autowired
    SurveyAnswerService surveyAnswerService;
    @Autowired
    private RestAPIService restAPIService;

    private MockWebServer mockWebServer;

    @Autowired
    private SurveyAnswerRepository surveyAnswerRepository;
    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

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

    @Test
    @DisplayName("설문 참여 컨트롤러 테스트")
    void participateSurvey() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/answer/external/load/1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult);
    }

    @Test
    void createResponse() throws Exception {
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

        ObjectMapper mapper = new ObjectMapper();
        String surveyResponse = mapper.writeValueAsString(surveyResponseDto);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/answer/external/response/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(surveyResponse))
                .andReturn();

        System.out.println("mvcResult = " + mvcResult);
    }

    @Test
    void readResponse() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/answer/external/response/1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult);

        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/answer/internal/getQuestionAnswerByCheckAnswerId/1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult2);
    }
}