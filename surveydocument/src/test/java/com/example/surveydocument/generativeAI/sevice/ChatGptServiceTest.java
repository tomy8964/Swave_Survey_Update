package com.example.surveydocument.generativeAI.sevice;

import com.example.surveydocument.generativeAI.request.*;
import com.example.surveydocument.restAPI.WebClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ChatGptServiceTest {

    private static MockWebServer mockBackEnd;
    private final ObjectMapper mapper = new ObjectMapper();
    private ChatGptService chatGptService;

    @BeforeEach
    void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(WebClientConfig.logRequest())
                .build();
        chatGptService = new ChatGptService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("ChatGPT 질문 후 응답 리스트 변환 테스트")
    void chatGptResult() throws Exception {
        // given
        ChatGptQuestionRequestDto chatGptQuestionRequestDto = new ChatGptQuestionRequestDto();
        chatGptQuestionRequestDto.setQuestion("ChatGPT 질문입니다.");
        List<ChatGptChoice> choiceList = new ArrayList<>();
        ChatGptChoice choice1 = ChatGptChoice.builder()
                .index(1)
                .message(new Message("role", "content1"))
                .finishReason("종료 이유입니다.")
                .build();
        ChatGptChoice choice2 = ChatGptChoice.builder()
                .index(2)
                .message(new Message("role", "content2"))
                .finishReason("종료 이유입니다.")
                .build();
        choiceList.add(choice1);
        choiceList.add(choice2);

        ChatGptResponseDto chatGptResponseDto = ChatGptResponseDto.builder()
                .choices(choiceList)
                .build();

        ChatResultDto expectedResponse = ChatResultDto.builder()
                .index(1)
                .text("ChatGPT 답변입니다.")
                .finishReason("종료 이유입니다.")
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setBody(mapper.writeValueAsString(chatGptResponseDto))
                .addHeader("Content-Type", "application/json"));

        // when
        Mono<ChatResultDto> actualResponseMono = chatGptService.chatGptResult(chatGptQuestionRequestDto);

        // then
        ChatResultDto actualResponse = actualResponseMono.block();
        assertEquals(choice1.getIndex(), actualResponse.getIndex());
        assertEquals(choice1.getMessage().getContent(), actualResponse.getText());
        assertEquals(choice1.getFinishReason(), actualResponse.getFinishReason());


        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
    }
}
