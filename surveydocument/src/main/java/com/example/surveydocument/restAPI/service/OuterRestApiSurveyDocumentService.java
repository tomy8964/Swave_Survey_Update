package com.example.surveydocument.restAPI.service;

import com.example.surveydocument.survey.domain.QuestionAnswer;
import com.example.surveydocument.survey.domain.Survey;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reference
 * https://findmypiece.tistory.com/276
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OuterRestApiSurveyDocumentService {

    @Value("${gateway.host}")
    private String gateway;
    private static String userInternalUrl = "/api/user/internal";

    // Current User 정보 가져오기
    public Long getCurrentUserFromUser(HttpServletRequest request) {
        String jwtHeader = ((HttpServletRequest)request).getHeader("Authorization");
        // WebClient 가져오기
        log.info("현재 유저정보를 가져옵니다");
        WebClient webClient = WebClient.create();

        // Current User URL
        String getCurrentUserUrl = "http://" + gateway + userInternalUrl + "/me";

        final Long[] userCode = new Long[1];

        userCode[0]=webClient.get()
                .uri(getCurrentUserUrl)
                .header("Authorization", jwtHeader)
                .retrieve()
                .bodyToMono(Long.class)
                .block();
//                .subscribe(response -> {
//                    userCode[0] = response;
//                });


//        final User[] getUser = new User[1];
//
//        webClient.get()
//                .uri(getCurrentUserUrl)
//                .header("Authorization", jwtHeader)
//                .retrieve()
//                .bodyToMono(User.class)
//                .subscribe(response -> {
//                    getUser[0] = User.builder()
//                            .id(response.getId())
//                            .email(response.getEmail())
//                            .nickname(response.getNickname())
//                            .userRole(response.getUserRole())
//                            .provider(response.getProvider())
//                            .survey(response.getSurvey())
//                            .build();
//                });


        // check log
        log.info("현재 유저의 설문 정보: " + userCode[0]);

        return userCode[0];
    }

    // User 에 Survey 정보 보내기
    public void sendSurveyToUser(HttpServletRequest request,Survey survey) {
        String jwtHeader = ((HttpServletRequest)request).getHeader("Authorization");
        // WebClient 가져오기
        log.info("Survey 정보를 보냅니다");
        WebClient webClient = WebClient.create();

        // Target URL
        String saveSurveyUrl = "http://" + gateway + userInternalUrl + "/survey/save";

        webClient.post()
                .uri(saveSurveyUrl)
                .header("Authorization", jwtHeader)
                .bodyValue(survey);

        log.info(survey.getUserCode() +"에게 정보 보냅니다");
    }

    // Answer Id 값을 통해 Question Answer 불러오기
    public List<QuestionAnswer> getQuestionAnswersByCheckAnswerId(Long id) {
        //REST API로 분석 시작 컨트롤러로 전달
        // Create a WebClient instance
        log.info("GET questionAnswer List by checkAnswerId");
        WebClient webClient = WebClient.create();

        // Define the API URL
        String apiUrl = "http://"+ gateway +"/api/answer/internal/getQuestionAnswerByCheckAnswerId/"+ id;

        // Make a GET request to the API and retrieve the response
        List<QuestionAnswer> questionAnswerList = webClient.get()
                .uri(apiUrl)
                .header("Authorization", "NotNull")
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        return mapper.readValue(responseBody, new TypeReference<List<QuestionAnswer>>() {});
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .blockOptional()
                .orElse(null);

        // Process the response as needed
        System.out.println("Request: " + questionAnswerList);

        return questionAnswerList;
    }
}
