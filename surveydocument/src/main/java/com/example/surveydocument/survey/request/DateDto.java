package com.example.surveydocument.survey.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateDto {
    private Date startDate;
    private Date endDate;
    private Boolean enable;
}
