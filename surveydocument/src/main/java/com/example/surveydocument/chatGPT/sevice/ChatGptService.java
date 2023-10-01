package com.example.surveydocument.chatGPT.sevice;


import com.example.surveydocument.chatGPT.config.ChatGptConfig;
import com.example.surveydocument.chatGPT.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Collections;

@Service
public class ChatGptService {

    private final RestTemplate restTemplate;

    public ChatGptService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpEntity<ChatGptRequestDto> buildHttpEntity(ChatGptRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(ChatGptConfig.MEDIA_TYPE));
        headers.add(ChatGptConfig.AUTHORIZATION, ChatGptConfig.BEARER + ChatGptConfig.API_KEY);
        return new HttpEntity<>(requestDto, headers);
    }

    private ChatGptChoice getResponse(HttpEntity<ChatGptRequestDto> chatGptRequestDtoHttpEntity) {
        ResponseEntity<ChatGptResponseDto> responseEntity = restTemplate.postForEntity(
                ChatGptConfig.URL,
                chatGptRequestDtoHttpEntity,
                ChatGptResponseDto.class);
        System.out.println("responseEntity = " + responseEntity);
        return responseEntity.getBody().getChoices().get(0);
    }

    private ChatGptChoice askQuestion(ChatGptQuestionRequestDto requestDto) {
        Message message = Message.builder()
                .role(ChatGptConfig.ROLE_USER)
                .content(requestDto.getQuestion())
                .build();
        System.out.println(message);
        return this.getResponse(
                this.buildHttpEntity(
                        new ChatGptRequestDto(
                                ChatGptConfig.MODEL,
                                Collections.singletonList(message)
                        )
                )
        );
    }

    public ChatResultDto chatGptResult(ChatGptQuestionRequestDto requestDto) {

        ChatGptChoice chatGptChoice=askQuestion(requestDto);
        return ChatResultDto.builder()
                .index(chatGptChoice.getIndex())
                .text(chatGptChoice.getMessage().getContent())
                .finishReason(chatGptChoice.getFinishReason())
                .build();
    }
}
