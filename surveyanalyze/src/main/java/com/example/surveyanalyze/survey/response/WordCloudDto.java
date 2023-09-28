package com.example.surveyanalyze.survey.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WordCloudDto {
    private Long id;
    private String title;
    private int count;
}
