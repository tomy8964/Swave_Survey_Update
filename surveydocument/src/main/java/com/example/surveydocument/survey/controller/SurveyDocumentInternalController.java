package com.example.surveydocument.survey.controller;

import com.example.surveydocument.restAPI.service.InterRestApiSurveyDocumentService;
import com.example.surveydocument.survey.domain.Choice;
import com.example.surveydocument.survey.domain.QuestionDocument;
import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.exception.InvalidTokenException;
import com.example.surveydocument.survey.repository.choice.ChoiceRepository;
import com.example.surveydocument.survey.repository.surveyDocument.SurveyDocumentRepository;
import com.example.surveydocument.survey.request.PageRequestDto;
import com.example.surveydocument.survey.request.SurveyRequestDto;
import com.example.surveydocument.survey.response.SurveyDetailDto;
import com.example.surveydocument.survey.response.WordCloudDto;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.redisson.RedissonRedLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@RequestMapping("/api/document/internal")
public class SurveyDocumentInternalController {

    private final SurveyDocumentService surveyService;
    private final InterRestApiSurveyDocumentService apiService;
    private final RedissonClient redissonClient;
    private final ChoiceRepository choiceRepository;
    private final SurveyDocumentRepository surveyDocumentRepository;

    @GetMapping(value = "/survey-list/{id}")
    public SurveyDetailDto readDetail(HttpServletRequest request, @PathVariable Long id) throws InvalidTokenException {
        return surveyService.readSurveyDetail(request, id);
    }

    @GetMapping(value = "/getSurveyDocument/{id}")
    public SurveyDocument readDetail(@PathVariable Long id) {
        return surveyService.getSurveyDocument(id);
    }

    // choice count ++
    @PostMapping(value = "/count/{id}")
    public void countChoice(@PathVariable Long id) throws Exception {
        surveyService.countChoice(id);

//        RedissonRedLock lock = new RedissonRedLock(redissonClient.getLock("choiceId"));
//
//        try {
//            if (lock.tryLock(1, 3, TimeUnit.SECONDS)) {
//                // transaction
//                surveyService.countChoice(id);
//                choiceRepository.flush();
//                return "count choice success";
//            } else {
//                throw new RuntimeException("Failed to acquire lock.");
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            lock.unlock();
//        }
    }

    // Survey Document 응답자 ++
    @GetMapping(value = "/countAnswer/{id}")
    public void countAnswer(@PathVariable Long id) throws Exception {
        surveyService.countSurveyDocument(id);
    }

    @GetMapping(value = "/getChoice/{id}")
    public Choice getChoice(@PathVariable Long id) {
        return surveyService.getChoice(id);
    }

    @GetMapping(value = "/getQuestion/{id}")
    public QuestionDocument getQuestion(@PathVariable Long id) {

        return surveyService.getQuestion(id);
    }

    @GetMapping(value = "/getQuestionByChoiceId/{id}")
    public QuestionDocument getQuestionByChoiceId(@PathVariable Long id) {
        return surveyService.getQuestionByChoiceId(id);
    }

    @PostMapping(value = "/setWordCloud/{id}")
    public void setWordCloud(@PathVariable Long id, @RequestBody List<WordCloudDto> wordCloudList) {
        RedissonRedLock lock = new RedissonRedLock(redissonClient.getLock("$surveyDocumentId"));

        try {
            if (lock.tryLock(1, 3, TimeUnit.SECONDS)) {
                // transaction
                surveyService.setWordCloud(id, wordCloudList);
            } else {
                throw new RuntimeException("Failed to acquire lock.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    // 유저 정보 저장하기
    @PostMapping(value = "/saveUser")
    public void saveUser(@RequestBody Long userCode) {
        apiService.saveUserInSurvey(userCode);
    }

}
