package com.example.surveydocument.survey.controller;

import com.example.surveydocument.survey.response.SurveyDetailDto;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@RequestMapping("/api/document/internal")
public class SurveyDocumentInternalController {

    private final SurveyDocumentService surveyService;

    @GetMapping(value = "/survey-list/{id}")
    public SurveyDetailDto readDetail(@PathVariable Long id) {
        return surveyService.readSurveyDetail(id);
    }

    @PostMapping(value = "/count/{id}")
    public void countChoice(@PathVariable Long id) {
        surveyService.countChoice(id);
    }

    // Survey Document 응답자 ++
    @GetMapping(value = "/countAnswer/{id}")
    public void countAnswer(@PathVariable Long id) throws Exception {
        surveyService.countSurveyDocument(id);
    }

//    @PostMapping(value = "/setWordCloud/{id}")
//    public void setWordCloud(@PathVariable Long id, @RequestBody List<WordCloudDto> wordCloudList) {
//        RedissonRedLock lock = new RedissonRedLock(redissonClient.getLock("$surveyDocumentId"));
//
//        try {
//            if (lock.tryLock(1, 3, TimeUnit.SECONDS)) {
//                // transaction
//                surveyService.setWordCloud(id, wordCloudList);
//            } else {
//                throw new RuntimeException("Failed to acquire lock.");
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            lock.unlock();
//        }
//    }
}
