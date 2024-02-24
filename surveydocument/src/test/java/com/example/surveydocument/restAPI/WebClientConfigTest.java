package com.example.surveydocument.restAPI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebClientConfig.class)
@ActiveProfiles("local")  // 'local' 프로파일 활성화
public class WebClientConfigTest {

    @Autowired
    private WebClient webClient;

    @Test
    void webClientNotNull() {
        // webClient 인스턴스가 생성되었는지 확인
        assertNotNull(webClient);
    }
}