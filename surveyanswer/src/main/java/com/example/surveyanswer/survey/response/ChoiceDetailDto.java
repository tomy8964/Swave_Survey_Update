package com.example.surveyanswer.survey.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ChoiceDetailDto implements Serializable {
    private Long id;
    private String title;
    private int count;

    @Builder
    public ChoiceDetailDto(Long id, String title, int count) {
        this.id = id;
        this.title = title;
        this.count = count;
    }
}
