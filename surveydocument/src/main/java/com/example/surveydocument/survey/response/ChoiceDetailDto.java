package com.example.surveydocument.survey.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceDetailDto {
    private Long id;
    private String title;
    private int count;

    // getter, setter 생략
}
