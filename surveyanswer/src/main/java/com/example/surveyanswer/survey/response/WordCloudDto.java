package com.example.surveyanswer.survey.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordCloudDto implements Serializable {
    private Long id;
    private String title;
    private int count;
}
