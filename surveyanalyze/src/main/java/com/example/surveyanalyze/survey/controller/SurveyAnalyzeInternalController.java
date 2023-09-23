package com.example.surveyanalyze.survey.controller;

import com.example.surveyanalyze.survey.service.SurveyAnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("api/analyze/internal")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class SurveyAnalyzeInternalController {

    private final SurveyAnalyzeService surveyService;
    private final RedissonClient redissonClient;

    @Autowired
    public SurveyAnalyzeInternalController(SurveyAnalyzeService surveyService, RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.surveyService = surveyService;
    }

    // 설문 분석 시작
    // 분산락 적용
    @PostMapping(value = "/research/analyze/create")
    public String saveAnalyze(@RequestBody String surveyId) {
        RedissonRedLock lock = new RedissonRedLock(redissonClient.getLock("/research/analyze/create"));

        try {
            if (lock.tryLock()) {
                // transaction
                surveyService.analyze(surveyId);
                surveyService.wordCloudPython(surveyId);
                return "Success";
            } else {
                throw new RuntimeException("Failed to acquire lock.");
            }
        } finally {
            lock.unlock();
        }
    }

    // 분산락 획득 테스트
    @GetMapping("/test-lock")
    public String testLock() {
        RLock lock = redissonClient.getLock("my-lock");

        try {
            if (lock.tryLock()) {
                // 분산락 획득 성공
                // 이곳에서 동시에 실행되면 안 되는 코드를 작성합니다
                Thread.sleep(5000); // 테스트를 위해 5초 동안 대기합니다.
                log.info("분산락 획득 성공");
                return "Lock acquired successfully!";
            } else {
                // 분산락 획득 실패
                log.info("분산락 획득 실패");
                return "Failed to acquire lock!";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Lock acquisition interrupted!");
            return "Lock acquisition interrupted!";
        } finally {
            lock.unlock();
        }
    }

    // 동시 요청 10개 분산락 테스트
    @GetMapping("/test-concurrent-requests")
    public String testConcurrentRequests() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                String result = testLock();
                System.out.println(result);
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("동시 요청 분산락 획득 실패");
            return "Test interrupted!";
        }

        return "All requests completed!";
    }
}
