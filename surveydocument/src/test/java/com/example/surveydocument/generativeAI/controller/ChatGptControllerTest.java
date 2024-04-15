package com.example.surveydocument.generativeAI.controller;

import com.example.surveydocument.generativeAI.request.ChatGptQuestionRequestDto;
import com.example.surveydocument.generativeAI.request.ChatResultDto;
import com.example.surveydocument.generativeAI.sevice.ChatGptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatGptController.class)
public class ChatGptControllerTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ChatGptService chatGptService;

    @Test
    @DisplayName("ChatGPT 질문 후 응답 테스트")
    public void sendQuestion() throws Exception {
        //given
        ChatGptQuestionRequestDto chatGptQuestionRequestDto = new ChatGptQuestionRequestDto();
        chatGptQuestionRequestDto.setQuestion("ChatGPT 질문입니다.");

        ChatResultDto chatResultDto = ChatResultDto.builder()
                .index(1)
                .text("ChatGPT 답변입니다.")
                .finishReason("종료 이유입니다.")
                .build();

        when(chatGptService.chatGptResult(any())).thenReturn(Mono.just(chatResultDto));

        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/document/external/chat-gpt/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatGptQuestionRequestDto))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        ResponseEntity<ChatResultDto> responseEntity = (ResponseEntity<ChatResultDto>) mvcResult.getAsyncResult();
        ChatResultDto actualResponse = responseEntity.getBody();

        //then
        assertNotNull(actualResponse, "MONO async result");
        assertEquals(chatResultDto.getIndex(), actualResponse.getIndex());
        assertEquals(chatResultDto.getText(), actualResponse.getText());
        assertEquals(chatResultDto.getFinishReason(), actualResponse.getFinishReason());
    }

}