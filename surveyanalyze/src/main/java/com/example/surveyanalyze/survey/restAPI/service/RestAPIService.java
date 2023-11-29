package com.example.surveyanalyze.survey.restAPI.service;

import com.example.surveyanalyze.survey.response.ChoiceDetailDto;
import com.example.surveyanalyze.survey.response.QuestionAnswerDto;
import com.example.surveyanalyze.survey.response.QuestionDetailDto;
import com.example.surveyanalyze.survey.response.SurveyDetailDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

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

    public SurveyDetailDto getSurveyDetailDto(Long surveyDocumentId) {
        log.info("Entering getSurveyDetailDto with surveyDocumentId: {}", surveyDocumentId);
        String apiUrl = buildApiUrl("/api/document/internal/getSurveyDocumentToAnalyze/" + surveyDocumentId);
        SurveyDetailDto surveyDetailDto = makeGetRequest(apiUrl, SurveyDetailDto.class);
        log.info("Response: " + surveyDetailDto + "\nExiting getSurveyDetailDto");

        return surveyDetailDto;
    }

    public ChoiceDetailDto getChoiceDto(Long choiceId) {
        log.info("Entering getChoiceDto with choiceId: {}", choiceId);
        String apiUrl = buildApiUrl("/api/document/internal/getChoice/" + choiceId);
        ChoiceDetailDto choiceDetailDto = makeGetRequest(apiUrl, ChoiceDetailDto.class);
        log.info("Response: " + choiceDetailDto + "\nExiting getChoiceDto");

        return choiceDetailDto;
    }

    public QuestionDetailDto getQuestionByChoiceId(Long choiceId) {
        log.info("Entering getQuestionByChoiceId with choiceId: {}", choiceId);
        String apiUrl = buildApiUrl("/api/document/internal/getQuestionByChoiceId/" + choiceId);
        QuestionDetailDto questionDetailDto = makeGetRequest(apiUrl, QuestionDetailDto.class);
        log.info("Response: " + questionDetailDto + "\nExiting getQuestionByChoiceId");

        return questionDetailDto;
    }

    public List<QuestionAnswerDto> getQuestionAnswerByCheckAnswerId(Long id) {
        log.info("Entering getQuestionAnswerByCheckAnswerId with CheckAnswerId: {}", id);
        String apiUrl = buildApiUrl("/api/answer/internal/getQuestionAnswerByCheckAnswerId/" + id);

        try {
            // Process the response as needed
            return webClient.get()
                    .uri(apiUrl)
                    .header("Authorization", "InterAPI")
                    .retrieve()
                    .bodyToMono(String.class)
                    .<List<QuestionAnswerDto>>handle((responseBody, sink) -> {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            sink.next(mapper.readValue(responseBody, new TypeReference<>() {
                            }));
                        } catch (JsonProcessingException e) {
                            sink.error(new RuntimeException(e));
                        }
                    })
                    .blockOptional()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error making GET request to {}: {}", apiUrl, e.getMessage());
            throw new RuntimeException("Error making GET request", e);
        }

    }

    private <T> T makeGetRequest(String apiUrl, Class<T> responseType) {
        log.info("Making GET request to: {}", apiUrl);

        try {
        return webClient.get()
                .uri(apiUrl)
                .header("Authorization", "InterAPI")
                .retrieve()
                .bodyToMono(responseType)
                .block();
        } catch (Exception e) {
            log.error("Error making GET request to {}: {}", apiUrl, e.getMessage());
            throw new RuntimeException("Error making GET request", e);
        }
    }

    private String buildApiUrl(String path) {
        return "http://" + gateway + path;
    }
}
