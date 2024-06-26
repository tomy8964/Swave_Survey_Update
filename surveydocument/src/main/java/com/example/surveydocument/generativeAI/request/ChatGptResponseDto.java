package com.example.surveydocument.generativeAI.request;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ChatGptResponseDto implements Serializable {
    private List<ChatGptChoice> choices;
}