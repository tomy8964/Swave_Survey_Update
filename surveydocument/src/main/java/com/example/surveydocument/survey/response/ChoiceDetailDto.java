package com.example.surveydocument.survey.response;

import com.example.surveydocument.survey.domain.Choice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceDetailDto {
    private Long id;
    private String title;
    private int count;

    public ChoiceDetailDto(Choice choice) {
        this.id = choice.getId();
        this.title = choice.getTitle();
        this.count = choice.getCount();
    }

    public static ChoiceDetailDto fromChoice(Choice choice) {
        return new ChoiceDetailDto(choice);
    }
}
