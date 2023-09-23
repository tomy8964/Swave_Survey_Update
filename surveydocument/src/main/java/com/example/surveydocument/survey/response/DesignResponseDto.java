package com.example.surveydocument.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DesignResponseDto {
    private String font;
    private int fontSize;
    private String backColor;

    @Builder
    public DesignResponseDto(String font, int fontSize, String backColor) {
        this.font = font;
        this.fontSize = fontSize;
        this.backColor = backColor;
    }
}
