package com.example.surveydocument.survey.request;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DesignRequestDto {
    private String font;
    private int fontSize;

    private String backColor;

    @Builder
    public DesignRequestDto(String font, int fontSize, String backColor) {
        this.font = font;
        this.fontSize = fontSize;
        this.backColor = backColor;
    }
}
