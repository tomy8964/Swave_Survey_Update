package com.example.surveydocument;

import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.repository.questionDocument.QuestionDocumentRepository;
import com.example.surveydocument.survey.repository.survey.SurveyRepository;
import com.example.surveydocument.survey.repository.surveyDocument.SurveyDocumentRepository;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockWebServer;
import org.hibernate.query.sqm.tree.SqmNode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Slf4j
public class RedisTest {
    @Autowired
    SurveyDocumentService documentService;
    @Autowired
    SurveyDocumentRepository documentRepository;
    @Autowired
    QuestionDocumentRepository questionDocumentRepository;
    @Autowired
    SurveyRepository surveyRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void clean() {
        documentRepository.deleteAll();
    }

    @Test @DisplayName("동시에 응답자 수 추가 100개")
    void Redis_test1() throws InterruptedException {
        // given
        SurveyDocument surveyDocument = SurveyDocument.builder()
                .title("설문 테스트")
                .description("설문 설명")
                .countAnswer(0)
                .build();
        documentRepository.save(surveyDocument);

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch countDownLatch = new CountDownLatch(100);

        // when
        for(int i = 0 ; i < 100; i++) {
            executorService.submit(() ->{
                try{
                    surveyRepository.surveyDocumentCount(surveyDocument);
                }
                finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        // then
        assertThat(documentRepository.findById(surveyDocument.getId()).get().getCountAnswer()).isEqualTo(100);
    }

    @Test @DisplayName("설문 응답자 수 1 추가할 때")
    @Transactional
    void Redis_test2() throws Exception {
        // given
        // 설문 생성
        SurveyDocument surveyDocument = SurveyDocument.builder()
                .title("설문 테스트")
                .description("설문 설명")
                .countAnswer(0)
                .build();
        documentRepository.save(surveyDocument);

        // when
        documentService.countSurveyDocument(surveyDocument.getId());

        // then
        assertThat(documentRepository.findById(surveyDocument.getId()).get().getCountAnswer()).isEqualTo(1);
    }

    @Test @DisplayName("Redis 조회수 증가 테스트")
    void Redis_test3() throws Exception {
        // given
        // init
        SurveyDocument surveyDocument = SurveyDocument.builder()
                .title("설문 테스트")
                .description("설문 설명")
                .countAnswer(0)
                .build();
        documentRepository.save(surveyDocument);

        // when
        int numberOfThread = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for(int i = 0 ; i < numberOfThread; i++) {
            executorService.submit(() -> {
                try {
                    documentService.countSurveyDocument(surveyDocument.getId());
                } catch (Exception e) {
                    log.error(Thread.currentThread().getName() + " 닫혔습니다" , e);
                }
            });
        }

        Thread.sleep(1000);
        // then
        executorService.shutdownNow();
        assertThat(documentRepository.findById(surveyDocument.getId()).get().getCountAnswer()).isEqualTo(100);
    }

}
