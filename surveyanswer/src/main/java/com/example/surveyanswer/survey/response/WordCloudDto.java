package com.example.surveyanswer.survey.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class WordCloudDto implements Serializable {
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
