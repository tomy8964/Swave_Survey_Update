package com.example.surveyanswer.survey.request;

import lombok.Builder;
import lombok.Data;

@Data
public class ReliabilityChoice {
    private int id;
    private String choiceName;

    @Builder
    public ReliabilityChoice(String choiceName) {
        this.choiceName = choiceName;
    }
}
