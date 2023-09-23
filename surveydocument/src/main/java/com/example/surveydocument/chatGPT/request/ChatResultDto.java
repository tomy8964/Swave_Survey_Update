package com.example.surveydocument.chatGPT.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResultDto {
    private String text;
    private Integer index;

    @JsonProperty("finish_reason")
    private String finishReason;

}
