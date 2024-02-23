package com.example.surveydocument.chatGPT.sevice;


import com.example.surveydocument.chatGPT.config.ChatGptConfig;
import com.example.surveydocument.chatGPT.request.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
public class ChatGptService {

    private WebClient webClient;

    public ChatGptService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(ChatGptConfig.URL).build();
    }

    // 테스트를 위한 메서드
    void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    private Mono<ChatGptResponseDto> getResponse(ChatGptRequestDto requestDto) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path(ChatGptConfig.URL).build())
                .header(ChatGptConfig.AUTHORIZATION, ChatGptConfig.BEARER + ChatGptConfig.API_KEY)
                .contentType(MediaType.parseMediaType(ChatGptConfig.MEDIA_TYPE))
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(ChatGptResponseDto.class);
    }

    private Mono<ChatGptChoice> askQuestion(ChatGptQuestionRequestDto requestDto) {
        Message message = Message.builder()
                .role(ChatGptConfig.ROLE_USER)
                .content(requestDto.getQuestion())
                .build();
        System.out.println(message);
        return this.getResponse(
                new ChatGptRequestDto(
                        ChatGptConfig.MODEL,
                        Collections.singletonList(message)
                )
        ).map(response -> response.getChoices().get(0));
    }

    public Mono<ChatResultDto> chatGptResult(ChatGptQuestionRequestDto requestDto) {
        return askQuestion(requestDto).map(chatGptChoice ->
                ChatResultDto.builder()
                        .index(chatGptChoice.getIndex())
                        .text(chatGptChoice.getMessage().getContent())
                        .finishReason(chatGptChoice.getFinishReason())
                        .build()
        );
    }
}
