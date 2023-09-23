package com.example.surveydocument.chatGPT.controller;


import com.example.surveydocument.chatGPT.request.ChatGptQuestionRequestDto;
import com.example.surveydocument.chatGPT.request.ChatResultDto;
import com.example.surveydocument.chatGPT.sevice.ChatGptService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/document/external")
public class ChatGptController {

    private final ChatGptService chatGptService;

    public ChatGptController(ChatGptService chatGptService) {
        this.chatGptService = chatGptService;
    }

    @PostMapping("/chat-gpt/question")
    public ChatResultDto sendQuestion(@RequestBody ChatGptQuestionRequestDto requestDto) {
        return chatGptService.chatGptResult(requestDto);
    }
}
