package com.example.surveyanswer.restAPI;

import com.example.surveyanswer.survey.repository.surveyAnswer.SurveyAnswerRepository;
import com.example.surveyanswer.survey.service.SurveyAnswerService;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;


import com.example.surveyanswer.survey.domain.SurveyDocument;
import com.example.surveyanswer.survey.repository.surveyAnswer.SurveyAnswerRepository;
import com.example.surveyanswer.survey.response.SurveyDetailDto;
import com.example.surveyanswer.survey.service.SurveyAnswerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.mockito.internal.matchers.Equals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RestTest {
    @Autowired
    SurveyAnswerService surveyAnswerService;
    @Autowired
    SurveyAnswerRepository surveyAnswerRepository;

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

    @Test
    void restTest() throws JsonProcessingException {
//        //given
//        SurveyDocument response = SurveyDocument.builder()
//                .title("Mock Test")
//                .description("REST API 테스트용 설문")
//                .build();
//
//        mockWebServer.enqueue(new MockResponse()
//                .setBody(objectMapper.writeValueAsString(response))
//                .addHeader("Content-Type", "application/json"));

        //when
        SurveyDetailDto participantSurvey = surveyAnswerService.getParticipantSurvey(1L);

        //then
        assertEquals(participantSurvey.getId(),1L);
        assertEquals(participantSurvey.getTitle(),"설문 제목");
    }
}
