package com.example.surveyanalyze.survey.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class SurveyManageDto {
    private boolean acceptResponse;
    private Date startDate;
    private Date deadline;
    private String url;
}
