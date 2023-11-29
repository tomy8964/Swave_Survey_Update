package com.example.surveyanswer.survey.restAPI.service;

import com.example.surveyanswer.survey.response.SurveyDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestAPIService {

    private final WebClient webClient;
    @Value("${gateway.host}")
    private final String gateway;

    @Autowired
    public RestAPIService(WebClient.Builder webClientBuilder, @Value("${gateway.host}") String gateway) {
        this.webClient = webClientBuilder.baseUrl(gateway).build();
        this.gateway = gateway;
    }

    public RestAPIService setGateway(String baseUrl, WebClient.Builder webClientBuilder) {
        return new RestAPIService(webClientBuilder.baseUrl(baseUrl).build(), baseUrl);
    }

    public void startAnalyze(Long surveyDocumentId) {
        log.info("Entering startAnalyze with surveyDocumentId: {}", surveyDocumentId);
        String apiUrl = buildApiUrl("/api/analyze/internal/research/analyze/create");
        String post = makePostRequest(apiUrl, String.valueOf(surveyDocumentId));
        log.info("Request: " + post + "\nExiting startAnalyze");
    }

    public void giveChoiceIdToCount(Long choiceId) {
        if (choiceId != null) {
            log.info("Entering giveChoiceIdToCount with choiceId: {}", choiceId);
            String apiUrl = buildApiUrl("/api/document/internal/count/" + choiceId);
            String post = makePostRequest(apiUrl, String.valueOf(choiceId));
            log.info("Request: " + post + "\nExiting giveChoiceIdToCount");
        }
    }

    public void giveDocumentIdtoCountResponse(Long surveyDocumentId) {
        if (surveyDocumentId != null) {
            log.info("Entering giveDocumentIdtoCountResponse with surveyDocumentId: {}", surveyDocumentId);
            String apiUrl = buildApiUrl("/api/document/internal/countAnswer/" + surveyDocumentId);
            String post = makePostRequest(apiUrl, String.valueOf(surveyDocumentId));
            log.info("Request: " + post + "\nExiting giveDocumentIdtoCountResponse");

        }
    }

    public SurveyDetailDto getSurveyDetailDto(Long surveyDocumentId) {
        log.info("Entering getSurveyDetailDto with surveyDocumentId: {}", surveyDocumentId);
        String apiUrl = buildApiUrl("/api/document/internal/getSurveyDocumentToAnswer/" + surveyDocumentId);

        try {
            SurveyDetailDto response = webClient.get()
                    .uri(apiUrl)
                    .header("Authorization", "NotNull")
                    .retrieve()
                    .bodyToMono(SurveyDetailDto.class)
                    .block();
            log.info("Response: " + response + "\nExiting getSurveyDetailDto");
            return response;
        } catch (Exception e) {
            log.error("Error making GET request to {}: {}", apiUrl, e.getMessage());
            throw new RuntimeException("Error making GET request", e);
        }


    }

    private String makePostRequest(String apiUrl, Object body) {
        try {
            return webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "InterAPI")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Error making POST request to {}: {}", apiUrl, e.getMessage());
            throw new RuntimeException("Error making POST request", e);
        }
    }

    private String buildApiUrl(String path) {
        return "http://" + gateway + path;
    }
}
