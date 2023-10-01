package com.example.surveydocument.survey.repository.surveyDocument;

import com.example.surveydocument.survey.domain.DateManagement;
import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.response.SurveyPageDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional
@SpringBootTest
public class SurveyDocumentRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    SurveyDocumentRepository surveyDocumentRepository;

    @Test
    public void searchPageSimple() {
        for (int i = 0; i < 100; i++) {
            DateManagement date = DateManagement.builder()
                    .startDate(new Date())
                    .build();
            SurveyDocument surveyDocument = SurveyDocument.builder()
                    .userId(1L)
                    .title(String.valueOf(i))
                    .build();
            surveyDocument.setDate(date);
            em.persist(surveyDocument);
        }

        PageRequest pageRequest = PageRequest.of(2, 10);

        Page<SurveyPageDto> surveyPageDtos = surveyDocumentRepository.pagingSurvey(1L, "date", "ascending", pageRequest);
        for (SurveyPageDto surveyPageDto : surveyPageDtos) {
            System.out.println("surveyPageDto = " + surveyPageDto);
        }
    }

}