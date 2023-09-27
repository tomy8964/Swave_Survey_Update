package com.example.surveyanalyze.survey.restAPI.service;

import com.example.surveyanalyze.survey.response.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestAPIService {

    public WebClient webClient = WebClient.create();
    @Value("${gateway.host}")
    private String gateway;

    public void setGateway(String baseUrl) {
        this.gateway = baseUrl;
        this.webClient = WebClient.create(gateway);
    }

    public SurveyDetailDto getSurveyDetailDto(Long surveyDocumentId) {
        //REST API로 분석 시작 컨트롤러로 전달
        // Create a WebClient instance
        log.info("GET SurveyDocument");
        WebClient webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .build();

        // Define the API URL
        String apiUrl = "http://" + gateway + "/api/document/internal/getSurveyDocument/" + surveyDocumentId;

        // Make a GET request to the API and retrieve the response
        SurveyDetailDto result = webClient.get()
                .uri(apiUrl)
                .header("Authorization", "NotNull")
                .retrieve()
                .bodyToMono(SurveyDetailDto.class)
                .block();

        // Process the response as needed
        System.out.println("Request: " + result);

        return result;
    }

    public ChoiceDetailDto getChoiceDto(Long choiceId) {
        //REST API로 분석 시작 컨트롤러로 전달
        // Create a WebClient instance
        log.info("GET Choice");
        WebClient webClient = WebClient.create();

        // Define the API URL
        String apiUrl = "http://" + gateway + "/api/document/internal/getChoice/" + choiceId;

        // Make a GET request to the API and retrieve the response
        ChoiceDetailDto result = webClient.get()
                .uri(apiUrl)
                .header("Authorization", "NotNull")
                .retrieve()
                .bodyToMono(ChoiceDetailDto.class)
                .block();

        // Process the response as needed
        System.out.println("Request: " + result);

        return result;
    }

    public QuestionDetailDto getQuestionDocument(Long questionId) {
        //REST API로 분석 시작 컨트롤러로 전달
        // Create a WebClient instance
        log.info("GET question");
        WebClient webClient = WebClient.create();

        // Define the API URL
        String apiUrl = "http://" + gateway + "/api/document/internal/getQuestion/" + questionId;

        // Make a GET request to the API and retrieve the response
        QuestionDetailDto result = webClient.get()
                .uri(apiUrl)
                .header("Authorization", "NotNull")
                .retrieve()
                .bodyToMono(QuestionDetailDto.class)
                .block();

        // Process the response as needed
        System.out.println("Request: " + result);

        return result;
    }

    public QuestionDetailDto getQuestionByChoiceId(Long choiceId) {
        //REST API로 분석 시작 컨트롤러로 전달
        // Create a WebClient instance
        log.info("GET question by choiceId");
        WebClient webClient = WebClient.create();

        // Define the API URL
        String apiUrl = "http://" + gateway + "/api/document/internal/getQuestionByChoiceId/" + choiceId;

        // Make a GET request to the API and retrieve the response
        QuestionDetailDto get = webClient.get()
                .uri(apiUrl)
                .header("Authorization", "NotNull")
                .retrieve()
                .bodyToMono(QuestionDetailDto.class)
                .block();

        // Process the response as needed
        System.out.println("Request: " + get);

        return get;
    }

    public List<QuestionAnswerDto> getQuestionAnswerByCheckAnswerId(Long id) {
        //REST API로 분석 시작 컨트롤러로 전달
        // Create a WebClient instance
        log.info("GET questionAnswer List by checkAnswerId");
        WebClient webClient = WebClient.create();

        // Define the API URL
        String apiUrl = "http://" + gateway + "/api/answer/internal/getQuestionAnswerByCheckAnswerId/" + id;

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
                        throw new RuntimeException(e);
                    }
                })
                .blockOptional()
                .orElse(null);

        // Process the response as needed
        System.out.println("Request: " + questionAnswerList);

        return questionAnswerList;
    }

    public void postToQuestionToSetWordCloud(Long id, List<WordCloudDto> wordCloudList) {
        //REST API로 분석 시작 컨트롤러로 전달
        // Create a WebClient instance
        log.info("GET question by choiceId");
        WebClient webClient = WebClient.create();

        // Define the API URL
        String apiUrl = "http://" + gateway + "/api/document/internal/setWordCloud/" + id;

        // Make a GET request to the API and retrieve the response
        String response = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "NotNull")
                .bodyValue(wordCloudList)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Process the response as needed
        System.out.println("Request: " + response);
    }
}
