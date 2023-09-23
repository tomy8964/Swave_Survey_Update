package com.example.surveydocument.survey.request;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
public class DateDto {
    Date startDate;
    Date endDate;

    @Builder
    public DateDto(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
