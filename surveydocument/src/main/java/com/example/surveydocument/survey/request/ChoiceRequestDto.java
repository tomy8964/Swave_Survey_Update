package com.example.surveydocument.survey.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChoiceRequestDto {
    private String choiceName;

    @Builder
    public ChoiceRequestDto(String choiceName) {
        this.choiceName = choiceName;
    }
}
