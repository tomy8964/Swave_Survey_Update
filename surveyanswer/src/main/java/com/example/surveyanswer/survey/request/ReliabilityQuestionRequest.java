package com.example.surveyanswer.survey.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReliabilityQuestionRequest {
    private List<ReliabilityQuestion> reliabilityQuestionList;
}
