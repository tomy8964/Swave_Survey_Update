package com.example.surveydocument.survey.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestDto {
    private String method; // grid or list
    private int page; // now page
    private String sort1; // date or title
    private String sort2; // ascending or descending
}
