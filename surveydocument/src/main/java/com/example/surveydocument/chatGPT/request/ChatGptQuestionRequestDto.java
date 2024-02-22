package com.example.surveydocument.chatGPT.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatGptQuestionRequestDto implements Serializable {
    private String question;
}
