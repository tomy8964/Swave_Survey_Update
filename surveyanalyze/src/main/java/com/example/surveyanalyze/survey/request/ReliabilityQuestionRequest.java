package com.example.surveyanalyze.survey.request;

import lombok.Data;

import java.util.List;

@Data
public class ReliabilityQuestionRequest {
    private List<ReliabilityQuestion> questionRequest;
}
