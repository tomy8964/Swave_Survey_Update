package com.example.surveydocument.survey.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
public class ManagementResponseDto {
    private Date startDate;
    private Date endDate;
    private Boolean enable;

    @Builder
    public ManagementResponseDto(Date startDate, Date endDate, Boolean enable) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.enable = enable;
    }
}
