package com.example.surveydocument.survey.controller;

import com.example.surveydocument.restAPI.service.RestApiService;
import com.example.surveydocument.survey.repository.choice.ChoiceRepository;
import com.example.surveydocument.survey.repository.date.DateRepository;
import com.example.surveydocument.survey.repository.design.DesignRepository;
import com.example.surveydocument.survey.repository.questionDocument.QuestionDocumentRepository;
import com.example.surveydocument.survey.repository.surveyDocument.SurveyDocumentRepository;
import com.example.surveydocument.survey.repository.wordCloud.WordCloudRepository;
import com.example.surveydocument.survey.request.*;
import com.example.surveydocument.survey.response.QuestionAnswerDto;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {

    @Autowired
    /*
      웹 API 테스트할 때 사용
      스프링 MVC 테스트의 시작점
      HTTP GET,POST 등에 대해 API 테스트 가능
      */
            MockMvc mockMvc;

    //    @Autowired
    SurveyDocumentService surveyDocumentService;
    @Autowired
    SurveyDocumentRepository surveyDocumentRepository;
    @Autowired
    DesignRepository designRepository;
    @Autowired
    QuestionDocumentRepository questionDocumentRepository;
    @Autowired
    ChoiceRepository choiceRepository;
    @Autowired
    WordCloudRepository wordCloudRepository;
    @Autowired
    DateRepository dateRepository;
    @Autowired
    RestApiService apiService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    PlatformTransactionManager transactionManager;
    private MockWebServer mockWebServer;

    private static SurveyRequestDto createSurveyRequestDto() {
        DesignRequestDto design = DesignRequestDto.builder()
                .font("폰트")
                .fontSize(1)
                .backColor("검은색").build();

        List<ChoiceRequestDto> choiceRequestDtoList = new ArrayList<>();
        ChoiceRequestDto choiceRequest1 = ChoiceRequestDto.builder()
                .choiceName("객관식 1번")
                .build();
        choiceRequestDtoList.add(choiceRequest1);
        ChoiceRequestDto choiceRequest2 = ChoiceRequestDto.builder()
                .choiceName("객관식 2번")
                .build();
        choiceRequestDtoList.add(choiceRequest2);

        List<QuestionRequestDto> questionRequestDtoList = new ArrayList<>();
        QuestionRequestDto questionRequest1 = QuestionRequestDto.builder()
                .title("객관식 설문 문항")
                .type(1)
                .choiceList(choiceRequestDtoList)
                .build();

        QuestionRequestDto questionRequest2 = QuestionRequestDto.builder()
                .title("주관식 설문 문항")
                .choiceList(null)
                .type(0)
                .build();
        questionRequestDtoList.add(questionRequest1);
        questionRequestDtoList.add(questionRequest2);

        SurveyRequestDto surveyRequest = SurveyRequestDto.builder()
                .title("설문 제목")
                .description("설문 내용")
                .type(0)
                .reliability(true)
                .startDate(new Date())
                .enable(true)
                .questionRequest(questionRequestDtoList)
                .design(design)
                .build();

        return surveyRequest;
    }

    @BeforeEach
    void setUp() throws IOException {
        // Create Survey Document
        ObjectMapper objectMapper = new ObjectMapper();

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
                if (path.startsWith("/api/user/internal/me")) {
                    String id;
                    try {
                        id = objectMapper.writeValueAsString(1L);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    response.setResponseCode(200)
                            .setBody(id)
                            .addHeader("Content-Type", "application/json");
                } else if (path.startsWith("/api/answer/internal/getQuestionAnswerByCheckAnswerId")) {
                    System.out.println("/api/answer/internal/getQuestionAnswerByCheckAnswerId");
                    List<QuestionAnswerDto> list = new ArrayList<>();
                    QuestionAnswerDto questionAnswer1 = QuestionAnswerDto.builder()
                            .questionType(0)
                            .id(3L)
                            .checkAnswer("주관식 답변")
                            .build();
                    QuestionAnswerDto questionAnswer2 = QuestionAnswerDto.builder()
                            .questionType(1)
                            .id(3L)
                            .checkAnswer("객관식 답변")
                            .build();
                    list.add(questionAnswer1);
                    list.add(questionAnswer2);
                    String answerList;
                    try {
                        answerList = objectMapper.writeValueAsString(list);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    response.setResponseCode(200)
                            .setBody(answerList)
                            .addHeader("Content-Type", "application/json");
                }
//                else if (path.startsWith("/api/document/internal/getSurveyDocumentToAnswer/2")) {
//                    System.out.println("getSurveyDocumentToAnswer");
//                    response.setResponseCode(200)
//                            .setBody(surveyReliability)
//                            .addHeader("Content-Type", "application/json");
//                } else {
//                    response.setResponseCode(200);
//                }

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
        surveyDocumentService = new SurveyDocumentService(surveyDocumentRepository, designRepository, questionDocumentRepository, choiceRepository, wordCloudRepository, dateRepository, apiService, redissonClient, transactionManager);

        // Update the base URL of the service to use the mock server
        apiService.setGateway(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Shutdown the MockWebServer
        mockWebServer.shutdown();
    }

    @Test
    @Transactional
    public void createAndUpdate() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String surveyRequest = mapper.writeValueAsString(createSurveyRequestDto());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(surveyRequest))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult);

        MvcResult mvcResult3 = mockMvc.perform(MockMvcRequestBuilders.get("/api/document/external/survey-list/1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult3);


        MvcResult mvcResult4 = mockMvc.perform(MockMvcRequestBuilders.get("/api/document/external/management/1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult4);

        String enable = mapper.writeValueAsString(Boolean.FALSE);
        MvcResult mvcResult5 = mockMvc.perform(MockMvcRequestBuilders.patch("/api/document/external/management/enable/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(enable))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult5);

        MvcResult mvcResult6 = mockMvc.perform(MockMvcRequestBuilders.get("/api/document/external/survey/count/1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult6);

        DateDto build = DateDto.builder()
                .startDate(new Date())
                .endDate(new Date())
                .build();
        String dateRequest = mapper.writeValueAsString(build);
        MvcResult mvcResult7 = mockMvc.perform(MockMvcRequestBuilders.patch("/api/document/external/management/date/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dateRequest))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult7);

        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        surveyRequestDto.setTitle("update");
        String surveyRequest2 = mapper.writeValueAsString(surveyRequestDto);
        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.put("/api/document/external/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(surveyRequest2))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult2);


        MvcResult mvcResult8 = mockMvc.perform(MockMvcRequestBuilders.patch("/api/document/external/delete/1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult8);


        PageRequestDto pageRequestDto = PageRequestDto.builder()
                .method("list")
                .page(2)
                .sort1("title")
                .sort2("ascending")
                .build();
        String pageRequest = mapper.writeValueAsString(pageRequestDto);

        MvcResult mvcResult9 = mockMvc.perform(MockMvcRequestBuilders.post("/api/document/external/survey-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pageRequest))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult9);
    }

    @Test
    @Transactional
    @CacheEvict(allEntries = true, value = {"survey", "survey2", "choice", "question", "getQuestionByChoiceId"})
    public void internal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String surveyRequest = mapper.writeValueAsString(createSurveyRequestDto());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(surveyRequest))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult);

        MvcResult mvcResult3 = mockMvc.perform(MockMvcRequestBuilders.post("/api/document/internal/count/1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult3);

        MvcResult mvcResult4 = mockMvc.perform(MockMvcRequestBuilders.get("/api/document/internal/countAnswer/1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult4);

        MvcResult mvcResult5 = mockMvc.perform(MockMvcRequestBuilders.get("/api/document/internal/getSurveyDocumentToAnswer/2"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult5);

        MvcResult mvcResult6 = mockMvc.perform(MockMvcRequestBuilders.get("/api/document/internal/getSurveyDocumentToAnalyze/2"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult6);

        MvcResult mvcResult7 = mockMvc.perform(MockMvcRequestBuilders.post("/api/document/internal/countAnswer/2"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult7);

        MvcResult mvcResult8 = mockMvc.perform(MockMvcRequestBuilders.get("/api/document/internal/getChoice/3"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult8);

        MvcResult mvcResult9 = mockMvc.perform(MockMvcRequestBuilders.get("/api/document/internal/getQuestion/3"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult9);

        MvcResult mvcResult10 = mockMvc.perform(MockMvcRequestBuilders.get("/api/document/internal/getQuestionByChoiceId/3"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult10);
    }


}