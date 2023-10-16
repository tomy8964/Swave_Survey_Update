package com.example.surveyanalyze.survey.controller;

import com.example.surveyanalyze.survey.response.ChoiceDetailDto;
import com.example.surveyanalyze.survey.response.QuestionAnswerDto;
import com.example.surveyanalyze.survey.response.QuestionDetailDto;
import com.example.surveyanalyze.survey.response.SurveyDetailDto;
import com.example.surveyanalyze.survey.restAPI.service.RestAPIService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class SurveyAnalyzeExternalControllerTest {

    @Autowired
    /*
      웹 API 테스트할 때 사용
      스프링 MVC 테스트의 시작점
      HTTP GET,POST 등에 대해 API 테스트 가능
      */
            MockMvc mockMvc;
    @Autowired
    private RestAPIService restAPIService;
    private MockWebServer mockWebServer;

    private static SurveyDetailDto createSurveyDetailDto() {
        SurveyDetailDto surveyDetailDto = SurveyDetailDto.builder()
                .id(1L)
                .title("설문 제목")
                .description("설문 설명")
                .countAnswer(100)
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
                .questionType(1)
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

        QuestionDetailDto questionDetailDto3 = QuestionDetailDto.builder()
                .id(1L)
                .title("Question 3 - 주관식")
                .questionType(0)
                .build();

        questionDetailDtoList.add(questionDetailDto1);
        questionDetailDtoList.add(questionDetailDto2);
        questionDetailDtoList.add(questionDetailDto3);

        surveyDetailDto.setQuestionList(questionDetailDtoList);

        return surveyDetailDto;
    }


    @BeforeEach
    void setUp() throws IOException {
        // Create Survey Document
        ObjectMapper objectMapper = new ObjectMapper();

        SurveyDetailDto surveyDetailDto = createSurveyDetailDto();
        String survey = objectMapper.writeValueAsString(surveyDetailDto);

        List<QuestionDetailDto> questionList = surveyDetailDto.getQuestionList();
        QuestionDetailDto questionDocument1 = questionList.get(0);
        List<ChoiceDetailDto> choiceList1 = questionDocument1.getChoiceList();
        String choice1 = objectMapper.writeValueAsString(choiceList1.get(0));
        String choice2 = objectMapper.writeValueAsString(choiceList1.get(1));

        QuestionDetailDto questionDocument2 = questionList.get(1);
        List<ChoiceDetailDto> choiceList2 = questionDocument2.getChoiceList();
        String choice3 = objectMapper.writeValueAsString(choiceList2.get(0));
        String choice4 = objectMapper.writeValueAsString(choiceList2.get(1));

        List<String> choiceStringList = new ArrayList<>();
        choiceStringList.add(choice1);
        choiceStringList.add(choice2);
        choiceStringList.add(choice3);
        choiceStringList.add(choice4);

//        List<ChoiceDetailDto> choiceList = new ArrayList<>();
//        choiceList.add(choiceList1.get(0));
//        choiceList.add(choiceList1.get(1));
//        choiceList.add(choiceList2.get(0));
//        choiceList.add(choiceList2.get(1));

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
                if (Objects.requireNonNull(path).startsWith("/api/document/internal/getChoice/")) {
                    String id = path.substring("/api/document/internal/getChoice/".length()); // Extract the value of {id}
                    String choice = choiceStringList.get(Integer.parseInt(id) - 1);
                    // Rest of your logic using the extracted id
                    response.setResponseCode(200)
                            .setBody(choice)
                            .addHeader("Content-Type", "application/json");
                } else if (path.startsWith("/api/document/internal/getSurveyDocumentToAnalyze/")) {
                    response.setResponseCode(200)
                            .setBody(survey)
                            .addHeader("Content-Type", "application/json");
                } else if (path.startsWith("/api/answer/internal/getQuestionAnswerByCheckAnswerId/")) {
                    String id = path.substring("/api/answer/internal/getQuestionAnswerByCheckAnswerId/".length()); // Extract the value of {id}
                    // Create a QuestionAnswerDto instance
                    QuestionAnswerDto questionAnswerDto1 = QuestionAnswerDto.builder()
                            .id(Long.valueOf(id))
                            .checkAnswer("주관식 답변" + id)
                            .questionType(0)
                            .build();
                    QuestionAnswerDto questionAnswerDto2 = QuestionAnswerDto.builder()
                            .id(Long.valueOf(id))
                            .checkAnswer("주관식 답변" + id)
                            .questionType(0)
                            .build();
                    QuestionAnswerDto questionAnswerDto3 = QuestionAnswerDto.builder()
                            .id(Long.valueOf(id))
                            .checkAnswer("주관식 답변" + id)
                            .questionType(0)
                            .build();
                    List<QuestionAnswerDto> questionAnswerDtoList = new ArrayList<>();
                    questionAnswerDtoList.add(questionAnswerDto1);
                    questionAnswerDtoList.add(questionAnswerDto2);
                    questionAnswerDtoList.add(questionAnswerDto3);
                    // Convert the QuestionAnswerDto to a JSON string
                    String jsonBody;
                    try {
                        jsonBody = new ObjectMapper().writeValueAsString(questionAnswerDtoList);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    // Create a MockResponse and set the body as the JSON string
                    response = new MockResponse()
                            .setResponseCode(200)
                            .setBody(jsonBody)
                            .addHeader("Content-Type", "application/json");
                } else if (path.startsWith("/api/document/internal/getQuestionByChoiceId/")) {
                    String choiceId = path.substring("/api/document/internal/getQuestionByChoiceId/".length()); // Extract the value of {id}

                    QuestionDetailDto question = questionDocument2;
                    if (Long.parseLong(choiceId) < 3) {
                        question = questionDocument1;
                    }
                    // Rest of your logic using the extracted id
                    try {
                        response.setResponseCode(200)
                                .setBody(objectMapper.writeValueAsString(question))
                                .addHeader("Content-Type", "application/json");
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
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

        // Update the base URL of the service to use the mock server
        restAPIService.setGateway(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Shutdown the MockWebServer
        mockWebServer.shutdown();
    }

    @Test
    void readSurvey() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/analyze/external/research/survey/load/-1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult);
    }

    @Test
    void readDetailAnalyze() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/analyze/external/research/analyze/-1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult);
    }

    @Test
    void startAnalyze() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/analyze/internal/research/analyze/create")
                        .content("1"))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult);
    }

}