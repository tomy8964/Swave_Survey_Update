package com.example.surveydocument.restAPI.service;

import com.example.surveydocument.survey.response.QuestionAnswerDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestApiService {

    private static String userInternalUrl = "/api/user/internal";
    private WebClient webClient;
    @Value("${gateway.host}")
    private String gateway;

    public RestApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(gateway).build();
    }

    // 테스트를 위한 메서드
    void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    // Current User 정보 가져오기
    public Optional<Long> getCurrentUserFromJWTToken(HttpServletRequest request) {
        String jwtHeader = request.getHeader("Authorization");
        // WebClient 가져오기
        log.info("현재 유저정보를 가져옵니다");

        // Current User URL
        String getCurrentUserUrl = userInternalUrl + "/me";

        Long userId = webClient.get()
                .uri(getCurrentUserUrl)
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
                .bodyToMono(String.class)
                .map(responseBody -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        return mapper.readValue(responseBody, new TypeReference<List<QuestionAnswerDto>>() {
                        });
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("응답을 QuestionAnswerDto로 변환하는 데 실패했습니다.", e);
                    }
                })
                .block();

        // Process the response as needed
        System.out.println("Request: " + questionAnswerList);

        return questionAnswerList;
    }
}
