package com.example.user.web;

import com.example.user.util.web.WebClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class WebClientConfigTest {

    @InjectMocks
    private WebClientConfig webClientConfig;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void webClientBeanCreationTest() {
        WebClient webClient = webClientConfig.webClient();
        assertNotNull(webClient);
    }
}