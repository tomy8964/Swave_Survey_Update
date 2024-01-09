package com.example.surveydocument.survey.service;

import com.example.surveydocument.restAPI.service.RestApiService;
import com.example.surveydocument.survey.domain.Choice;
import com.example.surveydocument.survey.domain.QuestionDocument;
import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.domain.WordCloud;
import com.example.surveydocument.survey.repository.choice.ChoiceRepository;
import com.example.surveydocument.survey.repository.date.DateRepository;
import com.example.surveydocument.survey.repository.design.DesignRepository;
import com.example.surveydocument.survey.repository.questionDocument.QuestionDocumentRepository;
import com.example.surveydocument.survey.repository.surveyDocument.SurveyDocumentRepository;
import com.example.surveydocument.survey.repository.wordCloud.WordCloudRepository;
import com.example.surveydocument.survey.request.*;
import com.example.surveydocument.survey.response.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
public class ServiceTest {
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
    @DisplayName("설문 저장")
    void service_test_1() {
        // given
        SurveyRequestDto surveyRequest = createSurveyRequestDto();

        // when
        Long savedDocumentId = surveyDocumentService.createTest(1L, surveyRequest);
        SurveyDocument findSurvey = surveyDocumentRepository.findById(savedDocumentId).get();
        // then

        assertThat(surveyRequest.getTitle()).isEqualTo(findSurvey.getTitle());
        assertThat(surveyRequest.getDesign().getFont()).isEqualTo(findSurvey.getDesign().getFont());
        assertThat(surveyRequest.getStartDate()).isEqualTo(findSurvey.getDate().getStartDate());
        assertThat(surveyRequest.getQuestionRequest().get(0).getTitle()).isEqualTo(findSurvey.getQuestionDocumentList().get(0).getTitle());
    }

    @Test
    @DisplayName("설문 수정")
    @Transactional
    void service_test_2() {
        // given
        SurveyRequestDto surveyRequest = createSurveyRequestDto();
        surveyRequest.setTitle("Update Title");
        Long savedDocumentId = surveyDocumentService.createTest(1L, surveyRequest);

        // when
        surveyDocumentService.updateSurvey(null, surveyRequest, savedDocumentId);

        // then
        SurveyDocument updatedSurvey = surveyDocumentRepository.findById(savedDocumentId).get();
    }

    @Test
    @Transactional
    @DisplayName("설문 삭제")
    void service_test3() {
        // given
        SurveyRequestDto surveyRequest = createSurveyRequestDto();
        Long savedDocumentId = surveyDocumentService.createTest(1L, surveyRequest);

        // when
        surveyDocumentService.deleteSurvey(null ,savedDocumentId);

        // then
        List<SurveyDocument> all = surveyDocumentRepository.findAll();
    }

    @Test
    @Transactional
    @DisplayName("날짜 수정")
    void service_test4() {
        // given
        SurveyRequestDto surveyRequest = createSurveyRequestDto();
        Long savedDocumentId = surveyDocumentService.createTest(1L, surveyRequest);

        DateDto dateRequest = DateDto.builder()
                .startDate(new Date())
                .endDate(new Date())
                .build();
        // when
        surveyDocumentService.managementDate(savedDocumentId, dateRequest);
    }

    @Test
    @Transactional
    @DisplayName("설문 Enable Manage")
    void service_test5() {
        // given
        SurveyRequestDto surveyRequest = createSurveyRequestDto();
        Long savedDocumentId = surveyDocumentService.createSurvey(1L, surveyRequest);
        // when
        surveyDocumentService.managementEnable(savedDocumentId, true);
    }

    @Test
    @Transactional
    @DisplayName("설문 관리 Read")
    void service_test6() {
        // given
        SurveyRequestDto surveyRequest = createSurveyRequestDto();
        Long savedDocumentId = surveyDocumentService.createSurvey(1L, surveyRequest);
        // when
        ManagementResponseDto managementResponseDto = surveyDocumentService.managementSurvey(savedDocumentId);
        //then
        System.out.println("managementResponseDto = " + managementResponseDto);
        surveyDocumentService.managementSurvey(-1L);
    }

    @Test
    @Transactional
    @DisplayName("설문 ReadDto")
    void service_test7() {
        // given
        SurveyRequestDto surveyRequest = createSurveyRequestDto();
        Long savedDocumentId = surveyDocumentService.createSurvey(1L, surveyRequest);
        // when
        SurveyDetailDto surveyDetailDto = surveyDocumentService.readSurveyDetail(savedDocumentId);
        //then
        System.out.println("surveyDetailDto = " + surveyDetailDto);
    }

    @Test
    @Transactional
    @DisplayName("createSurvey")
    public void service_test8() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer yourJwtTokenHere");
        SurveyRequestDto surveyRequest = createSurveyRequestDto();
        Long surveyId = surveyDocumentService.createSurvey(request, surveyRequest);
        ChoiceDetailDto choice = surveyDocumentService.getChoice(surveyDocumentRepository.findById(surveyId).get().getQuestionDocumentList().get(0).getChoiceList().get(0).getId());
        surveyDocumentService.getQuestion(16L);
        surveyDocumentService.getQuestion(15L);
        surveyDocumentService.getSurveyDocument(surveyId);
        surveyDocumentService.countChoice(choice.getId());
        surveyDocumentService.getQuestionByChoiceId(16L);
        surveyDocumentService.getQuestionByChoiceId(15L);
    }

    @Test
    @Transactional
    @DisplayName("wordCloud test")
    public void service_test9() {
        //given
        SurveyRequestDto surveyRequest = createSurveyRequestDto();
        Long surveyId = surveyDocumentService.createSurvey(1L, surveyRequest);
        SurveyDocument surveyDocument = surveyDocumentRepository.findById(surveyId).get();
        List<QuestionDocument> questionDocumentList = surveyDocument.getQuestionDocumentList();
        QuestionDocument questionDocument = questionDocumentList.get(1);
        WordCloud wordCloud1 = WordCloud.builder()
                .questionDocument(questionDocument)
                .title("주관식 답변1")
                .count(5)
                .build();
        WordCloud wordCloud2 = WordCloud.builder()
                .questionDocument(questionDocument)
                .title("주관식 답변2")
                .count(1)
                .build();
        questionDocument.getWordCloudList().add(wordCloud1);
        questionDocument.getWordCloudList().add(wordCloud2);
        //when
        SurveyDocument save = surveyDocumentRepository.save(surveyDocument);
        surveyDocumentRepository.flush();
        //then
        SurveyDetailDto surveyDetailDto = surveyDocumentService.readSurveyDetail(save.getId());
        System.out.println("surveyDetailDto = " + surveyDetailDto);
    }

    @Test
    public void readSurveyList() throws Exception {
        //given
        PageRequestDto request = PageRequestDto.builder()
                .method("list")
                .page(2)
                .sort1("date")
                .sort2("descending")
                .build();

        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addHeader("Authorization", "Bearer yourJwtTokenHere");
        //when
        Page<SurveyPageDto> surveyPageDtos = surveyDocumentService.readSurveyList(request2, request);

        //then
        for (SurveyPageDto surveyPageDto : surveyPageDtos) {
            System.out.println("surveyPageDto = " + surveyPageDto);
        }
        PageRequestDto request3 = PageRequestDto.builder()
                .method("list")
                .page(2)
                .sort1("date")
                .sort2("descending")
                .build();
        //when
        surveyDocumentService.readSurveyList(request2, request3);
        PageRequestDto request4 = PageRequestDto.builder()
                .method("list")
                .page(2)
                .sort1("title")
                .sort2("descending")
                .build();
        //when
        surveyDocumentService.readSurveyList(request2, request4);

        PageRequestDto request5 = PageRequestDto.builder()
                .method("list")
                .page(2)
                .sort1("title")
                .sort2("ascending")
                .build();
        //when
        surveyDocumentService.readSurveyList(request2, request5);

        SurveyPageDto title = new SurveyPageDto("title", new Date());
    }


}
