package com.example.surveyanswer.survey.restAPI.service;

import com.example.surveyanswer.survey.response.SurveyDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestAPIService {

    @Value("${gateway.host}")
    private String gateway;

    public WebClient webClient = WebClient.create();

    public void setGateway(String baseUrl) {
        this.gateway = baseUrl;
        this.webClient = WebClient.create(gateway);
    }


    public void startAnalyze(Long surveyDocumentId) {
        log.info("응답 저장 후 -> 분석 시작 REST API 전달");

        String apiUrl = "http://" + gateway + "/api/analyze/internal/research/analyze/create";
        log.info(apiUrl);

        String post = webClient.post()
                .uri(apiUrl)
                .header("Authorization","NouNull")
                .bodyValue(String.valueOf(surveyDocumentId))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Request: " + post);
    }

    public void giveChoiceIdToCount(Long choiceId) {
        if (choiceId != null) {
            log.info("응답 저장 후 -> choice count 전달");

            String apiUrl = "http://" + gateway + "/api/document/internal/count/" + choiceId;
            log.info(apiUrl);

            String post = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization","NouNull")
                    .bodyValue(String.valueOf(choiceId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }
        log.info("test choice count REST API 전달");
    }

    public void giveDocumentIdtoCountResponse(Long surveyDocumentId) {
        if (surveyDocumentId != null) {
            log.info("응답 저장 후 -> count answer 전달");

            // Define the API URL
            String apiUrl = "http://" + gateway + "/api/document/internal/countAnswer/" + surveyDocumentId;
            log.info(apiUrl);

            String post = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization","NouNull")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        }
        log.info("test 분석 시작 REST API 전달");
    }

    public SurveyDetailDto getSurveyDetailDto(Long surveyDocumentId) {
        log.info("GET SurveyDetailDto");

        String apiUrl = "http://" + gateway + "/api/document/internal/getSurveyDocumentToAnswer/" + surveyDocumentId;
        log.info(apiUrl);

        SurveyDetailDto response = webClient.get()
                .uri(apiUrl)
                .header("Authorization","NotNull")
                .retrieve()
                .bodyToMono(SurveyDetailDto.class)
                .block();

        return response;
    }
}
