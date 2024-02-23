package com.example.user.restAPI;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {
    private final ObjectMapper objectMapper;

    public WebClientConfig() {
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }

    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("connection-pool")
                .maxConnections(100)                    // connection pool의 갯수
                .pendingAcquireTimeout(Duration.ofMillis(0)) //커넥션 풀에서 커넥션을 얻기 위해 기다리는 최대 시간
                .pendingAcquireMaxCount(-1)             //커넥션 풀에서 커넥션을 가져오는 시도 횟수 (-1: no limit)
                .maxIdleTime(Duration.ofMillis(2000L))        //커넥션 풀에서 idle 상태의 커넥션을 유지하는 시간
                .build();
    }

    @Bean
    public WebClient webClient() {
        final int bufferSize = 16 * 1024 * 1024;  // 16MB
        final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs().maxInMemorySize(bufferSize);
                })
                .build();

        final HttpClient httpClient = HttpClient.create(connectionProvider())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)  // Connection timeout
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))  // Read timeout
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS))  // Write timeout
                );

        final ExchangeFilterFunction errorHandlingFilter = ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            String errorMessage =
                                    String.format("Error response from server. " +
                                        "Status code: %s, body: %s, headers: %s",
                                        clientResponse.statusCode(), errorBody, clientResponse.headers().asHttpHeaders());
                            return Mono.error(new RuntimeException(errorMessage));
                        });
            }
            return Mono.just(clientResponse);
        });


        return WebClient.builder()
                .exchangeStrategies(exchangeStrategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(errorHandlingFilter)
                .build();
    }
}
