package com.example.surveyanswer.survey.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceDetailDto implements Serializable {
    private Long id;
    private String title;
    private int count;

    // getter, setter 생략
}
