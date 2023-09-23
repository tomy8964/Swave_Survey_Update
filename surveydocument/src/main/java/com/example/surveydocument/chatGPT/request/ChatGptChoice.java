package com.example.surveydocument.chatGPT.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatGptChoice implements Serializable {

    private Integer index;

    private Message message;

    @JsonProperty("finish_reason")
    private String finishReason;
}
