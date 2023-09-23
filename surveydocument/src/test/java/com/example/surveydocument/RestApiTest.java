package com.example.surveydocument;

import com.example.surveydocument.restAPI.service.InterRestApiSurveyDocumentService;
import com.example.surveydocument.restAPI.service.OuterRestApiSurveyDocumentService;
import com.example.surveydocument.survey.domain.Survey;
import com.example.surveydocument.survey.repository.survey.SurveyRepository;
import com.example.surveydocument.survey.repository.surveyDocument.SurveyDocumentRepository;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import com.example.surveydocument.util.OAuth.JwtProperties;
import okhttp3.mockwebserver.MockWebServer;
import org.aspectj.bridge.Message;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.*;
import java.io.IOException;
import java.util.ArrayList;

@SpringBootTest
public class RestApiTest {
    @Autowired
    SurveyDocumentService surveyDocumentService;
    @Autowired
    SurveyDocumentRepository surveyDocumentRepository;
    @Autowired
    OuterRestApiSurveyDocumentService apiService;
    @Autowired
    InterRestApiSurveyDocumentService interApiService;
    @Autowired
    SurveyRepository surveyRepository;

    private static MockWebServer mockWebServer;
    String host;

    @BeforeAll
    static void startApiServer() throws IOException {
        // 가짜 api server 만들기
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutApiServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        host = String.format(
                "http://localhost:%s", mockWebServer.getPort()
        );
    }

    @Test @DisplayName("현재 유저 정보 받아오기 Test")
    void API_test1() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", JwtProperties.HEADER_STRING);

        // when
        Long getUser = apiService.getCurrentUserFromUser(request);

        // then
        assertThatThrownBy(() -> apiService.getCurrentUserFromUser(request)).hasMessage(String.valueOf(Message.ERROR));
    }

    @Test @DisplayName("User에 Survey 정보 보내기")
    void API_test2() {
//        // given
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.addHeader("Authorization", JwtProperties.HEADER_STRING);
//
//        Survey survey = Survey.builder()
//                .surveyDocumentList(new ArrayList<>())
//                .user(new User())
//                .build();
//
//        // when
//        apiService.sendSurveyToUser(request, survey);

    }

    @Test @DisplayName("saveUserInSurvey")
    void API_test3() {
        Long check = 1L;
        interApiService.saveUserInSurvey(check);
        Survey byUserCode = surveyRepository.findByUserCode(check);

        assertThat(check).isEqualTo(byUserCode.getUserCode());
    }
}
