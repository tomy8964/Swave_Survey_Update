package com.example.surveydocument.restAPI;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    public final ObjectMapper OM = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());
    @Bean
    public WebClient webClient() {
        final int size = 16 * 1024 * 1024;
        final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(OM, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(OM, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs().maxInMemorySize(size);
                }) // No size limit
                .build();

        return WebClient.builder()
                .exchangeStrategies(exchangeStrategies)
                .build();
    }
//    @Bean
//    public WebClient webClient() {
//        final int size = 16 * 1024 * 1024;
//        final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
//                .codecs(configurer -> configurer.defaultCodecs()
//                        .maxInMemorySize(size)) // No size limit
//                .build();
//
//        return WebClient.builder()
//                .exchangeStrategies(exchangeStrategies)
//                .build();
//    }
}
