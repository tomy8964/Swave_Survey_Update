package com.example.user.restAPI.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OuterRestApiUserService {
    private static final String surveyDocumentInternalUrl = "/api/document/internal";
    @Value("${gateway.host}")
    private String gateway;

    // void => 비동기식으로 변경
    // Document 에 유저 정보 보내기
    public void sendUserToSurveyDocument(Long userCode) {
        log.info("Document 에 User 정보를 보냅니다");

        WebClient webClient = WebClient.create();
        String documentUrl = "http://" + gateway + surveyDocumentInternalUrl + "/saveUser";

        webClient.post()
                .uri(documentUrl)
                .header("Authorization", "NotNull")
                .bodyValue(userCode)
                .retrieve()
                .bodyToMono(Long.class)
                .subscribe(((List<Long>) new ArrayList<Long>())::add);

        log.info(userCode + " 정보를 Document에 보냅니다");
    }
}
