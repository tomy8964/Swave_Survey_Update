package com.example.surveyanalyze.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WordCloudDto {
    private Long id;
    private String title;
    private int count;

    @Builder
    public WordCloudDto(Long id, String title, int count) {
        this.id = id;
        this.title = title;
        this.count = count;
    }
}
