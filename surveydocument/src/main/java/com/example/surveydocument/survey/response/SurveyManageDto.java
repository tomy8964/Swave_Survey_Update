package com.example.surveydocument.survey.response;

import jakarta.persistence.Column;
import lombok.Builder;
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

    @Builder
    public SurveyManageDto(boolean acceptResponse, Date startDate, Date deadline, String url) {
        this.acceptResponse = acceptResponse;
        this.startDate = startDate;
        this.deadline = deadline;
        this.url = url;
    }
}
