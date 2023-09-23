package com.example.surveyanswer.survey.restAPI.service;

import com.example.surveyanswer.survey.domain.SurveyDocument;
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


    public void restAPItoAnalyzeController(Long surveyDocumentId) {
        //REST API로 분석 시작 컨트롤러로 전달
        // Create a WebClient instance
        log.info("응답 저장 후 -> 분석 시작 REST API 전달");

        // Define the API URL
        String apiUrl = "http://" + gateway + "/api/analyze/internal/research/analyze/create";

        log.info(apiUrl);

        // Make a GET request to the API and retrieve the response
        String post = webClient.post()
                .uri(apiUrl)
                .header("Authorization","NouNull")
                .bodyValue(String.valueOf(surveyDocumentId))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Process the response as needed
        System.out.println("Request: " + post);
    }

    public void giveChoiceIdToCount(Long choiceId) {
        if (choiceId != null) {
            //REST API로 분석 시작 컨트롤러로 전달
            // Create a WebClient instance
            log.info("응답 저장 후 -> choice count 전달");

            // Define the API URL
            String apiUrl = "http://" + gateway + "/api/document/internal/count/";
            log.info(apiUrl);

            // Make a GET request to the API and retrieve the response
            webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(apiUrl+"{id}")
                            .build(choiceId));
//            String post = webClient.post()
//                    .uri(apiUrl)
//                    .header("Authorization","NouNull")
//                    .bodyValue(String.valueOf(choiceId))
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();
        }
        log.info("test choice count REST API 전달");
    }

    public void giveDocumentIdtoCountAnswer(Long surveyDocumentId) {
        if (surveyDocumentId != null) {
            //REST API로 분석 시작 컨트롤러로 전달
            // Create a WebClient instance
            log.info("응답 저장 후 -> count answer 전달");

            // Define the API URL
            String apiUrl = "http://" + gateway + "/api/document/internal/countAnswer/";
            log.info(apiUrl);

            // Make a GET request to the API and retrieve the response
            webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(apiUrl+"{id}")
                            .build(surveyDocumentId));
//            String post = webClient.get()
//                    .uri(apiUrl)
//                    .header("Authorization","NouNull")
//                    .bodyValue(String.valueOf(surveyDocumentId))
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();

        }
        log.info("test 분석 시작 REST API 전달");
    }

    public SurveyDocument getSurveyDocument(Long surveyDocumentId) {
        //REST API로 분석 시작 컨트롤러로 전달
        // Create a WebClient instance
        log.info("GET SurveyDocument");

        // Define the API URL
        String apiUrl = "http://" + gateway + "/api/document/internal/getSurveyDocument/" + surveyDocumentId;
        log.info(apiUrl);

        // Make a GET request to the API and retrieve the response
        SurveyDocument get = webClient.get()
                .uri(apiUrl)
                .header("Authorization","NotNull")
                .retrieve()
                .bodyToMono(SurveyDocument.class)
                .block();

        // Process the response as needed
        System.out.println("Request: " + get.getDesign());

        return get;
    }
}
