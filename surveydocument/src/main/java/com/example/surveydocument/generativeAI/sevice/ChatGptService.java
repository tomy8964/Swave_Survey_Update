package com.example.surveydocument.generativeAI.sevice;


import com.example.surveydocument.generativeAI.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static com.example.surveydocument.generativeAI.config.ChatGptConfig.*;

@Service
@RequiredArgsConstructor
public class ChatGptService implements GenerativeAIService {

    private final WebClient webClient;

    private Mono<ChatGptResponseDto> getResponse(ChatGptRequestDto requestDto) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path(URL).build())
                .header(AUTHORIZATION, BEARER + API_KEY)
                .contentType(MediaType.parseMediaType(MEDIA_TYPE))
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(ChatGptResponseDto.class);
    }

    private Mono<ChatGptChoice> askQuestion(QuestionRequestDto requestDto) {
        Message message = Message.builder()
                .role(ROLE_USER)
                .content(requestDto.getQuestion())
                .build();
        System.out.println(message);
        return this.getResponse(
                new ChatGptRequestDto(
                        MODEL,
                        Collections.singletonList(message)
                )
        ).map(response -> response.getChoices().get(0));
    }

    @Override
    public Mono<ChatResultDto> chatResult(QuestionRequestDto requestDto) {
        return askQuestion(requestDto).map(chatGptChoice ->
                ChatResultDto.builder()
                        .index(chatGptChoice.getIndex())
                        .text(chatGptChoice.getMessage().getContent())
                        .finishReason(chatGptChoice.getFinishReason())
                        .build()
        );
    }
}
