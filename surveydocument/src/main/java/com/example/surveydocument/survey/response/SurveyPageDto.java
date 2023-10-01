package com.example.surveydocument.survey.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
public class SurveyPageDto {
    private String title;
    private Date startDate;

    @QueryProjection
    public SurveyPageDto(String title, Date startDate) {
        this.title = title;
        this.startDate = startDate;
    }
}

