package com.example.surveydocument.survey.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompareAnalyzeDto {
    private Long id;
    private String questionTitle;
    private Double pValue;
}
