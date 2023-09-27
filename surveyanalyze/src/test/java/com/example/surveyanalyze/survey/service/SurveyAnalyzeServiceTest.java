package com.example.surveyanalyze.survey.service;

import com.example.surveyanalyze.ResourceLocator;
import com.example.surveyanalyze.survey.repository.aprioriAnlayze.AprioriAnalyzeRepository;
import com.example.surveyanalyze.survey.repository.chiAnlayze.ChiAnalyzeRepository;
import com.example.surveyanalyze.survey.repository.choiceAnalyze.ChoiceAnalyzeRepository;
import com.example.surveyanalyze.survey.repository.compareAnlayze.CompareAnalyzeRepository;
import com.example.surveyanalyze.survey.repository.questionAnlayze.QuestionAnalyzeRepository;
import com.example.surveyanalyze.survey.repository.surveyAnalyze.SurveyAnalyzeRepository;
import com.example.surveyanalyze.survey.response.ChoiceDetailDto;
import com.example.surveyanalyze.survey.response.QuestionDetailDto;
import com.example.surveyanalyze.survey.response.SurveyDetailDto;
import com.example.surveyanalyze.survey.restAPI.service.RestAPIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SurveyAnalyzeServiceTest {

    //    @Autowired
    private SurveyAnalyzeService surveyAnalyzeService;
    @Autowired
    private ChiAnalyzeRepository chiAnalyzeRepository;
    @Autowired
    private CompareAnalyzeRepository compareAnalyzeRepository;
    @Autowired
    private AprioriAnalyzeRepository aprioriAnalyzeRepository;
    @Autowired
    private ChoiceAnalyzeRepository choiceAnalyzeRepository;
    @Autowired
    private QuestionAnalyzeRepository questionAnalyzeRepository;
    @Autowired
    private SurveyAnalyzeRepository surveyAnalyzeRepository;
    @Autowired
    private RestAPIService restAPIService;
    private MockWebServer mockWebServer;
    private ResourceLocator resourceLocator;

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

        List<ChoiceDetailDto> choiceList = new ArrayList<>();
        choiceList.add(choiceList1.get(0));
        choiceList.add(choiceList1.get(1));
        choiceList.add(choiceList2.get(0));
        choiceList.add(choiceList2.get(1));

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
                if (path.startsWith("/api/document/internal/getChoice/")) {
                    String id = path.substring("/api/document/internal/getChoice/".length()); // Extract the value of {id}
                    String choice = choiceStringList.get(Integer.parseInt(id) - 1);
                    // Rest of your logic using the extracted id
                    response.setResponseCode(200)
                            .setBody(choice)
                            .addHeader("Content-Type", "application/json");
                } else if (path.startsWith("/api/document/internal/getSurveyDocument/")) {
                    response.setResponseCode(200)
                            .setBody(survey)
                            .addHeader("Content-Type", "application/json");
                } else if (path.startsWith("/api/answer/internal/getQuestionAnswerByCheckAnswerId/")) {
                    String id = path.substring("/api/answer/internal/getQuestionAnswerByCheckAnswerId/".length()); // Extract the value of {id}
                    // Rest of your logic using the extracted id
                    response.setResponseCode(200)
                            .setBody("word Cloud")
                            .addHeader("Content-Type", "application/json");
                }
                else if (path.startsWith("/api/document/internal/getQuestionByChoiceId/")) {
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
                }
                else {
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
        surveyAnalyzeService = new SurveyAnalyzeService(
                chiAnalyzeRepository,
                compareAnalyzeRepository,
                aprioriAnalyzeRepository,
                choiceAnalyzeRepository,
                questionAnalyzeRepository,
                surveyAnalyzeRepository,
                restAPIService,
                resourceLocator);

        // Update the base URL of the service to use the mock server
        restAPIService.setGateway(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Shutdown the MockWebServer
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Get SurveyDto from SurveyDocument Service")
    void getSurveyDetailDto() {
        SurveyDetailDto surveyDetailDto = surveyAnalyzeService.getSurveyDetailDto(1L);
    }

    @Test
    void countWords() {
    }

    @Test
    void analyze() {
        surveyAnalyzeService.analyze("-1");
    }

    @Test
    void wordCloud() {
    }

    @Test
    void readSurveyDetailAnalyze() {
    }

    @Test
    void wordCloudPython() {
    }
}