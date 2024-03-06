package com.example.user.web;

import okhttp3.mockwebserver.MockWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Profile("test")
@Configuration
public class TestWebClientConfig {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public MockWebServer mockWebServer() {
        return new MockWebServer();
    }

    @Bean
    public WebClient webClient(MockWebServer mockWebServer) {

        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
