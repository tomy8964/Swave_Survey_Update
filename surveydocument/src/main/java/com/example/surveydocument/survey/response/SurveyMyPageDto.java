package com.example.surveydocument.survey.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyMyPageDto {
    private Long id;
    private String title;
    private String description;
    private Date startDate;
    private Date deadline;


}
