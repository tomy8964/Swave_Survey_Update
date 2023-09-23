package com.example.user.restAPI;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(-1)) // No size limit
                .build();

        return WebClient.builder()
                .exchangeStrategies(exchangeStrategies)
                .build();
    }
}
