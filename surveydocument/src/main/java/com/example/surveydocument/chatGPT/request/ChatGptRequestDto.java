package com.example.surveydocument.chatGPT.request;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ChatGptRequestDto implements Serializable {

    private String model;
    private List<Message> messages;

}