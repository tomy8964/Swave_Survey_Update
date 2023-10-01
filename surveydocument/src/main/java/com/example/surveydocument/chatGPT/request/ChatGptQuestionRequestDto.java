package com.example.surveydocument.chatGPT.request;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
@Data
public class ChatGptQuestionRequestDto implements Serializable {
    private String question;
}
