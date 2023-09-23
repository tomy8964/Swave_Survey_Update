package com.example.surveyanalyze.survey.request;

import lombok.Data;

import java.util.List;

@Data
public class ReliabilityQuestion {
    private String title;
    private int type;
    private List<ReliabilityChoice> choiceList;
    private String correct_answer;

}
