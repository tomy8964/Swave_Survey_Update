package com.example.surveyanswer.survey.restAPI.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class RestAPIServiceTest {
    @Autowired
    RestAPIService restAPIService;

    @Test
    public void choiceId_Null_to_Count() {
        //given
        restAPIService.giveChoiceIdToCount(null);
        restAPIService.giveDocumentIdtoCountResponse(null);

        //when

        //then
    }

}