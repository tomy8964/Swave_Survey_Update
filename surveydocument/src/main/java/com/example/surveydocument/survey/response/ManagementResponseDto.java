package com.example.surveydocument.survey.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ManagementResponseDto {
    private Date startDate;
    private Date endDate;
    private Boolean enable;

    @QueryProjection
    @Builder
    public ManagementResponseDto(Date startDate, Date endDate, Boolean enable) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.enable = enable;
    }
}
