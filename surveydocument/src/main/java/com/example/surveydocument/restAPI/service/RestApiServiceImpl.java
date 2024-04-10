package com.example.surveydocument.restAPI.service;

import com.example.surveydocument.survey.response.QuestionAnswerDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestApiServiceImpl implements RestApiService {

    private final static String userInternalUrl = "api/user/internal";
    private final WebClient webClient;

    public Optional<Long> getCurrentUserFromJWTToken(HttpServletRequest request) {
        String jwtHeader = request.getHeader("Authorization");
        String getCurrentUserUrl = userInternalUrl + "/me";

        Long userId = webClient.get()
                .uri(uriBuilder -> uriBuilder.path(getCurrentUserUrl).build())
                .header("Authorization", jwtHeader)
                .retrieve()
                .bodyToMono(Long.class)
                .block();

        return Optional.ofNullable(userId);
    }

    // Answer Id 값을 통해 Question Answer 불러오기
    public List<QuestionAnswerDto> getQuestionAnswersByCheckAnswerId(Long id) {
        String apiUrl = "/api/answer/internal/getQuestionAnswerByCheckAnswerId/" + id;

        List<QuestionAnswerDto> questionAnswerList = webClient.get()
                .uri(apiUrl)
                .header("Authorization", "NotNull")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<QuestionAnswerDto>>() {
                })
                .block();

        return questionAnswerList;
    }
}
