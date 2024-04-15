package com.example.surveydocument.generativeAI.sevice;


import com.example.surveydocument.generativeAI.request.ChatResultDto;
import com.example.surveydocument.generativeAI.request.QuestionRequestDto;
import reactor.core.publisher.Mono;

public interface GenerativeAIService {

    Mono<ChatResultDto> chatResult(QuestionRequestDto requestDto);
}
