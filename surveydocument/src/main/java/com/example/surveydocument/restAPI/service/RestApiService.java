package com.example.surveydocument.restAPI.service;

import com.example.surveydocument.survey.response.QuestionAnswerDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestApiService {

    private final static String userInternalUrl = "api/user/internal";
    private final WebClient webClient;
    private final ObjectMapper mapper;

    // Current User 정보 가져오기
    public Optional<Long> getCurrentUserFromJWTToken(HttpServletRequest request) {
        String jwtHeader = request.getHeader("Authorization");
        // WebClient 가져오기
        log.info("현재 유저정보를 가져옵니다");

        // Current User URL
        String getCurrentUserUrl = userInternalUrl + "/me";

        Long userId = webClient.get()
                .uri(uriBuilder -> uriBuilder.path(getCurrentUserUrl).build())
                .header("Authorization", jwtHeader)
                .retrieve()
                .bodyToMono(Long.class)
                .block();


        // check log
        log.info("현재 유저의 ID: " + userId);

        return Optional.ofNullable(userId);
    }

    // Answer Id 값을 통해 Question Answer 불러오기
    public List<QuestionAnswerDto> getQuestionAnswersByCheckAnswerId(Long id) {
        //REST API로 분석 시작 컨트롤러로 전달
        // Create a WebClient instance
        log.info("GET questionAnswer List by checkAnswerId");

        // Define the API URL
        String apiUrl = "/api/answer/internal/getQuestionAnswerByCheckAnswerId/" + id;

        // Make a GET request to the API and retrieve the response
        List<QuestionAnswerDto> questionAnswerList = webClient.get()
                .uri(apiUrl)
                .header("Authorization", "NotNull")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<QuestionAnswerDto>>() {
                })
                .block();

        // Process the response as needed
        System.out.println("Request: " + questionAnswerList);

        return questionAnswerList;
    }
}
