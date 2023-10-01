package com.example.surveydocument.chatGPT.sevice;

import com.example.surveydocument.chatGPT.config.ChatGptConfig;
import com.example.surveydocument.chatGPT.request.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ChatGptServiceTest {

    @Autowired
    /*
      웹 API 테스트할 때 사용
      스프링 MVC 테스트의 시작점
      HTTP GET,POST 등에 대해 API 테스트 가능
      */
    MockMvc mockMvc;
    @Mock
    private RestTemplate restTemplate;

    @MockBean
    private ChatGptService chatGptService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatGptService = new ChatGptService(restTemplate);
    }

    @Test
    @Transactional
    public void testAskQuestion() {
        // Create a sample ChatGptRequestDto
        ChatGptQuestionRequestDto requestDto = new ChatGptQuestionRequestDto();
        requestDto.setQuestion("question");

        Message role1 = new Message("role", "0 content");
        ChatGptChoice finishReason1 = new ChatGptChoice(0, role1, "finish_reason");

        Message role2 = new Message("role", "1 content");
        ChatGptChoice finishReason2 = new ChatGptChoice(1, role2, "finish_reason");
        List<ChatGptChoice> list = new ArrayList<>();
        list.add(finishReason1);
        list.add(finishReason2);
        ChatGptResponseDto chatGptResponseDto = new ChatGptResponseDto(list);

        ResponseEntity<ChatGptResponseDto> mockResponseEntity = new ResponseEntity<>(chatGptResponseDto, HttpStatus.OK);
        System.out.println("mockResponseEntity.getBody() = " + mockResponseEntity.getBody());

        // Mock the restTemplate.postForEntity method to return the mockResponseEntity
        when(restTemplate.postForEntity(eq(ChatGptConfig.URL), any(HttpEntity.class), eq(ChatGptResponseDto.class)))
                .thenReturn(mockResponseEntity);

        // Call the method you want to test
        ChatResultDto chatResultDto = chatGptService.chatGptResult(requestDto);
        System.out.println("result = " + chatResultDto);
    }

    @Test
    @Transactional
    public void testAskQuestion2() throws Exception {
        // Create a sample ChatGptRequestDto
        ChatGptQuestionRequestDto requestDto = new ChatGptQuestionRequestDto();
        requestDto.setQuestion("question");

        Message role1 = new Message("role", "0 content");
        ChatGptChoice finishReason1 = new ChatGptChoice(0, role1, "finish_reason");

        Message role2 = new Message("role", "1 content");
        ChatGptChoice finishReason2 = new ChatGptChoice(1, role2, "finish_reason");
        List<ChatGptChoice> list = new ArrayList<>();
        list.add(finishReason1);
        list.add(finishReason2);
        ChatGptResponseDto chatGptResponseDto = new ChatGptResponseDto(list);

        ResponseEntity<ChatGptResponseDto> mockResponseEntity = new ResponseEntity<>(chatGptResponseDto, HttpStatus.OK);
        System.out.println("mockResponseEntity.getBody() = " + mockResponseEntity.getBody());

        // Mock the restTemplate.postForEntity method to return the mockResponseEntity
        when(restTemplate.postForEntity(eq(ChatGptConfig.URL), any(HttpEntity.class), eq(ChatGptResponseDto.class)))
                .thenReturn(mockResponseEntity);

        ObjectMapper mapper = new ObjectMapper();
        String request = mapper.writeValueAsString(requestDto);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/document/external/chat-gpt/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andReturn();
        System.out.println("mvcResult = " + mvcResult);
    }

}