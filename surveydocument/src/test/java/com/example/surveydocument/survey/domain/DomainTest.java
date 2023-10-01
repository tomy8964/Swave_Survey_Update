package com.example.surveydocument.survey.domain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DomainTest {
    @Test
    public void domain_test() throws Exception {
        Choice build = Choice.builder()
                .title("")
                .id(1L)
                .count(0)
                .isDeleted(false)
                .questionDocument(null)
                .build();
        QuestionDocument build1 = QuestionDocument.builder()
                .surveyDocument(null)
                .id(1L)
                .questionType(0)
                .choiceList(null)
                .isDeleted(false)
                .wordCloudList(null)
                .title("")
                .build();
        Design build2 = Design.builder()
                .surveyDocument(null)
                .backColor("")
                .fontSize(1)
                .font("")
                .build();
    }

}