package com.example.surveydocument.chatGPT.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@JsonSerialize(using = MessageSerializer.class)
@NoArgsConstructor
@Builder
public class Message implements Serializable {
    private String role;
    private String content;
    @JsonCreator
    public Message(@JsonProperty("role") String role, @JsonProperty("content") String content) {
        this.role=role;
        this.content=content;
    }


}