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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestApiServiceTest {

    private static MockWebServer mockBackEnd;
    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private RestApiServiceImpl restApiService;

    @BeforeEach
    void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(WebClientConfig.logRequest())
                .build();
        mapper = new ObjectMapper();
        restApiService = new RestApiServiceImpl(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("JWT Token으로 유저 정보 조회 테스트")
    void getCurrentUserFromJWTTokenTest() throws Exception {
        // given
        Long userId = 1L;
        mockBackEnd.enqueue(new MockResponse().setBody(userId.toString())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("testToken");

        // when
        Optional<Long> result = restApiService.getCurrentUserFromJWTToken(request);

        // then
        assertEquals(userId, result.get());

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/user/internal/me", recordedRequest.getPath());
    }

    @Test
    @DisplayName("선택지로 응답 목록 조회 성공")
    void getQuestionAnswersByCheckAnswerIdTest() throws Exception {
        // given
        List<QuestionAnswerDto> questionAnswerList = new ArrayList<>();
        questionAnswerList.add(new QuestionAnswerDto(1L, "test1", 0));
        questionAnswerList.add(new QuestionAnswerDto(2L, "test2", 0));

        mockBackEnd.enqueue(new MockResponse().setBody(mapper.writeValueAsString(questionAnswerList))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        // when
        List<QuestionAnswerDto> result = restApiService.getQuestionAnswersByCheckAnswerId(1L);

        // then
        assertEquals(questionAnswerList.size(), result.size());

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/answer/internal/getQuestionAnswerByCheckAnswerId/1", recordedRequest.getPath());
    }

    @Test
    @DisplayName("선택지로 응답 목록 조회 실패 - JSON Parsing Error")
    void getQuestionAnswersByCheckAnswerIdTest_JsonProcessingException() throws Exception {
        // given
        mockBackEnd.enqueue(new MockResponse()
                .setBody("This is not a valid JSON!")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // when & then
        assertThrows(DecodingException.class, () -> restApiService.getQuestionAnswersByCheckAnswerId(1L));
        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/answer/internal/getQuestionAnswerByCheckAnswerId/1", recordedRequest.getPath());
    }

}