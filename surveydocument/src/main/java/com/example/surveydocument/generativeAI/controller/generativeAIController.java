package com.example.surveydocument.generativeAI.controller;


import com.example.surveydocument.generativeAI.request.ChatResultDto;
import com.example.surveydocument.generativeAI.request.QuestionRequestDto;
import com.example.surveydocument.generativeAI.sevice.GenerativeAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/document/external")
public class generativeAIController {

    private final GenerativeAIService generativeAIService;

    @PostMapping("/chat-gpt/question")
    public Mono<ResponseEntity<ChatResultDto>> sendQuestion(@RequestBody QuestionRequestDto requestDto) {
        return generativeAIService.chatResult(requestDto)
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK));
    }
}
