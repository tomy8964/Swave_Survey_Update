package com.example.surveydocument.restAPI.service;

import com.example.surveydocument.restAPI.WebClientConfig;
import com.example.surveydocument.survey.response.QuestionAnswerDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class RestApiServiceTest {

    private static MockWebServer mockBackEnd;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private RestApiService restApiService;

    @BeforeEach
    void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(WebClientConfig.logRequest())
                .build();
        restApiService = new RestApiService(webClient, mapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("JWT Token으로 유저 정보 조회 테스트")
    void getCurrentUserFromJWTTokenTest() throws Exception {
        Long userId = 1L;
        mockBackEnd.enqueue(new MockResponse().setBody(mapper.writeValueAsString(userId))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("testToken");

        Optional<Long> result = restApiService.getCurrentUserFromJWTToken(request);
        assertEquals(userId, result.get());

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/user/internal/me", recordedRequest.getPath());
    }

    @Test
    @DisplayName("선택지로 응답 목록 조회 테스트")
    void getQuestionAnswersByCheckAnswerIdTest() throws Exception {
        List<QuestionAnswerDto> questionAnswerList = new ArrayList<>();
        questionAnswerList.add(new QuestionAnswerDto());
        questionAnswerList.add(new QuestionAnswerDto());

        mockBackEnd.enqueue(new MockResponse().setBody(mapper.writeValueAsString(questionAnswerList))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<QuestionAnswerDto> result = restApiService.getQuestionAnswersByCheckAnswerId(1L);
        assertEquals(questionAnswerList.size(), result.size());

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/answer/internal/getQuestionAnswerByCheckAnswerId/1", recordedRequest.getPath());
    }

    @Test
    @DisplayName("선택지로 응답 목록 조회 실패 - JSON Parsing Error")
    void getQuestionAnswersByCheckAnswerIdTest_JsonProcessingException() throws Exception {
        // 잘못된 JSON 형식의 응답을 설정합니다.
        mockBackEnd.enqueue(new MockResponse()
                .setBody("This is not a valid JSON!")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // RuntimeException이 발생하는지 검증합니다.
        assertThrows(RuntimeException.class, () -> restApiService.getQuestionAnswersByCheckAnswerId(1L));

        // 요청이 올바르게 이루어졌는지 검증합니다.
        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/answer/internal/getQuestionAnswerByCheckAnswerId/1", recordedRequest.getPath());
    }

}