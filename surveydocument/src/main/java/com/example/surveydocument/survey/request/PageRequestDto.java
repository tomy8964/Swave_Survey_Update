package com.example.surveydocument.survey.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PageRequestDto {
    private String method; // grid or list
    private int page; // now page
    private String sort1; // date or title
    private String sort2; // ascending or descending

    public PageRequestDto(String method, int page, String sort1, String sort2) {
        this.method = method;
        this.page = page;
        this.sort1 = sort1;
        this.sort2 = sort2;
    }
}
