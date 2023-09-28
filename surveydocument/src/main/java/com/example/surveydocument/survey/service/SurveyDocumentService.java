package com.example.surveydocument.survey.service;

import com.example.surveydocument.restAPI.service.OuterRestApiSurveyDocumentService;
import com.example.surveydocument.survey.domain.*;
import com.example.surveydocument.survey.repository.choice.ChoiceRepository;
import com.example.surveydocument.survey.repository.date.DateRepository;
import com.example.surveydocument.survey.repository.design.DesignRepository;
import com.example.surveydocument.survey.repository.questionDocument.QuestionDocumentRepository;
import com.example.surveydocument.survey.repository.surveyDocument.SurveyDocumentRepository;
import com.example.surveydocument.survey.repository.wordCloud.WordCloudRepository;
import com.example.surveydocument.survey.request.*;
import com.example.surveydocument.survey.response.*;
import com.example.surveydocument.util.page.PageRequest;
import com.sun.xml.messaging.saaj.packaging.mime.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

//import static com.example.surveyAnswer.util.SurveyTypeCheck.typeCheck;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableTransactionManagement
public class SurveyDocumentService {
    private final SurveyDocumentRepository surveyDocumentRepository;
    private final DesignRepository designRepository;
    private final QuestionDocumentRepository questionDocumentRepository;
    private final ChoiceRepository choiceRepository;
    private final WordCloudRepository wordCloudRepository;
    private final DateRepository dateRepository;
    private final OuterRestApiSurveyDocumentService apiService;
    // redis 분산 락 사용
    // 분산 락은 Transactional 과 같이 진행되지 않아서 따로 관리로직을 만들어야한다
    private final RedissonClient redissonClient;
    private final PlatformTransactionManager transactionManager;
    Random random = new Random();
    @Value("${gateway.host}")
    private String gateway;

    @Transactional
    public Long createSurvey(HttpServletRequest request, SurveyRequestDto surveyRequest) {
        // 유저 정보 받아오기
        // User Module 에서 현재 유저 가져오기
        Long userId = apiService.getCurrentUserFromUser(request);

        return createTest(userId, surveyRequest);
    }

    @Transactional
    public Long createTest(Long userId, SurveyRequestDto surveyRequest) {
        // Survey Request 를 Survey Document 에 저장하기
        SurveyDocument surveyDocument = SurveyDocument.builder()
                .userId(userId)
                .title(surveyRequest.getTitle())
                .description(surveyRequest.getDescription())
                .type(surveyRequest.getType())
                .reliability(surveyRequest.getReliability()) // 진정성 검사
                .countAnswer(0)
                .build();

        Design design = Design.builder()
                .surveyDocument(surveyDocument)
                .font(surveyRequest.getDesign().getFont())
                .fontSize(surveyRequest.getDesign().getFontSize())
                .backColor(surveyRequest.getDesign().getBackColor())
                .build();
        designRepository.save(design);

        DateManagement dateManagement = DateManagement.builder()
                .surveyDocument(surveyDocument)
                .startDate(surveyRequest.getStartDate())
                .deadline(surveyRequest.getEndDate())
                .isEnabled(surveyRequest.getEnable())
                .build();
        dateRepository.save(dateManagement);

        // 설문 문항
        for (QuestionRequestDto questionRequestDto : surveyRequest.getQuestionRequest()) {
            // 설문 문항 저장
            QuestionDocument questionDocument = QuestionDocument.builder()
                    .surveyDocument(surveyDocument)
                    .title(questionRequestDto.getTitle())
                    .questionType(questionRequestDto.getType())
                    .build();

            if (questionRequestDto.getType() == 0) continue; // 주관식

            // 객관식, 찬부식일 경우 선지 저장
            for (ChoiceRequestDto choiceRequestDto : questionRequestDto.getChoiceList()) {
                Choice choice = Choice.builder()
                        .questionDocument(questionDocument)
                        .title(choiceRequestDto.getChoiceName())
                        .count(0)
                        .build();
                choiceRepository.save(choice);
            }
            questionDocumentRepository.save(questionDocument);
        }
        surveyDocumentRepository.save(surveyDocument);

        return surveyDocument.getId();
    }

    // gird method 로 SurveyDocument 조회
//    public List<SurveyDocument> readSurveyListByGrid(HttpServletRequest request, PageRequestDto pageRequest) {
//
//        // User Module 에서 현재 유저 가져오기
//        Long userCode = apiService.getCurrentUserFromUser(request);
//
//        return null;
//    }
//
//    // list method 로 SurveyDocument 조회
//    public Page<SurveyDocument> readSurveyList(HttpServletRequest request, PageRequestDto pageRequest) {
//
//
//        // User Module 에서 현재 유저 가져오기
//        Long userCode = apiService.getCurrentUserFromUser(request);
//
//        PageRequest page = PageRequest.builder()
//                .page(pageRequest.getPage())
//                .method(pageRequest.getMethod())
//                .sortProperties(pageRequest.getSort1()) // date or title
//                .direct(pageRequest.getSort2()) // ascending or descending
//                .build();
//
//        // Request Method
//        // 1. view Method : grid or list
//        // 2. what page number
//        // 3. sort on What : date or title
//        // 4. sort on How : ascending or descending
//        Pageable pageable = page.of(page.getSortProperties(), page.getDirection(page.getDirect()));
//
//        return surveyRepository.surveyDocumentPaging(userCode, pageable);
//    }

    public SurveyDetailDto readSurveyDetail(Long id) {
        return getSurveyDetailDto(id);
    }

    public SurveyDocument getSurveyDocument(Long surveyDocumentId) {
        return surveyDocumentRepository.findById(surveyDocumentId).get();
    }

    public void countChoice(Long choiceId) throws Exception {
        // survey document id 값을 키로 하는 lock 을 조회합니다.
        RLock rLock = redissonClient.getLock("choice : lock");
        // Lock 획득 시도
        boolean isLocked = rLock.tryLock(5, 10, TimeUnit.SECONDS);

        log.info(Thread.currentThread().getName() + " lock 획득 시도!");

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info(Thread.currentThread().getName() + " Transaction 시작");

        // @Transactional 대신 코드로 트랜잭션을 관리한다
        try {
            if (!isLocked) {
                throw new MessagingException("failed to get RLock");
            }

            // 조회수 증가 로직 실행
            try {
                Choice getChoice = choiceRepository.findById(choiceId).orElse(null);
                getChoice.addCount();
                // 실행하면 커밋후 트랜잭션 종료
                transactionManager.commit(status);
                log.info(Thread.currentThread().getName() + " 커밋 후 트랜잭션 종료");
            } catch (RuntimeException e) {
                // 로직 실행 중 예외가 발생하면 롤백
                transactionManager.rollback(status);
                log.info(Thread.currentThread().getName() + " 로직 실행 실패");
                throw new Exception(e.getMessage());
            }

        } catch (InterruptedException e) {
            throw new Exception("Thread Interrupted");
        } finally {
            // 로직 수행이 끝나면 Lock 반환
            if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
                log.info(Thread.currentThread().getName() + " Lock 해제");
            }
        }
    }

    // 설문 응답자 수 + 1
    // 분산락 실행
    public void countSurveyDocument(Long surveyDocumentId) throws Exception {
        // survey document id 값을 키로 하는 lock 을 조회합니다.
        RLock rLock = redissonClient.getLock("survey : lock");
        // Lock 획득 시도
        boolean isLocked = rLock.tryLock(5, 10, TimeUnit.SECONDS);
        log.info(Thread.currentThread().getName() + " lock 획득 시도!");
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info(Thread.currentThread().getName() + " Transaction 시작");

        // @Transactional 대신 코드로 트랜잭션을 관리한다
        try {
            if (!isLocked) {
                throw new MessagingException("failed to get RLock");
            }

            // 조회수 증가 로직 실행
            try {
                SurveyDocument surveyDocument = surveyDocumentRepository.findById(surveyDocumentId).orElseThrow(null);
                surveyDocument.addCountAnswer();
                // 실행하면 커밋후 트랜잭션 종료
                transactionManager.commit(status);
                log.info(Thread.currentThread().getName() + " 커밋 후 트랜잭션 종료");
            } catch (RuntimeException e) {
                // 로직 실행 중 예외가 발생하면 롤백
                transactionManager.rollback(status);
                log.info(Thread.currentThread().getName() + " 로직 실행 실패");
                throw new Exception(e.getMessage());
            }

        } catch (InterruptedException e) {
            throw new Exception("Thread Interrupted");
        } finally {
            // 로직 수행이 끝나면 Lock 반환
            if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
                log.info(Thread.currentThread().getName() + " Lock 해제");
            }
        }
    }

    // SurveyDocument Response 보낼 SurveyDetailDto로 변환하는 메서드
    private SurveyDetailDto getSurveyDetailDto(Long surveyDocumentId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findById(surveyDocumentId).get();
        SurveyDetailDto surveyDetailDto = new SurveyDetailDto();

        // SurveyDocument에서 SurveyDetailDto로 데이터 복사
        surveyDetailDto.setId(surveyDocument.getId());
        surveyDetailDto.setTitle(surveyDocument.getTitle());
        surveyDetailDto.setDescription(surveyDocument.getDescription());
        surveyDetailDto.setReliability(surveyDocument.getReliability());

        // 디자인
        DesignResponseDto designResponse = new DesignResponseDto();
        designResponse.setFont(surveyDocument.getDesign().getFont());
        designResponse.setFontSize(surveyDocument.getDesign().getFontSize());
        designResponse.setBackColor(surveyDocument.getDesign().getBackColor());
        surveyDetailDto.setDesign(designResponse);

        // 날짜
        surveyDetailDto.setStartDate(surveyDocument.getDate().getStartDate());
        surveyDetailDto.setEndDate(surveyDocument.getDate().getDeadline());
        surveyDetailDto.setEnable(surveyDocument.getDate().getIsEnabled());

        List<QuestionDetailDto> questionDtos = new ArrayList<>();
        for (QuestionDocument questionDocument : surveyDocument.getQuestionDocumentList()) {
            QuestionDetailDto questionDto = new QuestionDetailDto();
            questionDto.setId(questionDocument.getId());
            questionDto.setTitle(questionDocument.getTitle());
            questionDto.setQuestionType(questionDocument.getQuestionType());

            // question type에 따라 choice 에 들어갈 내용 구분
            // 주관식이면 choice title에 주관식 응답을 저장??
            // 객관식 찬부식 -> 기존 방식 과 똑같이 count를 올려서 저장
            List<ChoiceDetailDto> choiceDtos = new ArrayList<>();
            if (questionDocument.getQuestionType() == 0) {
                // 주관식 답변들 리스트
                // REST API GET questionAnswersByCheckAnswerId
                List<QuestionAnswerDto> questionAnswerList = apiService.getQuestionAnswersByCheckAnswerId(questionDocument.getId());
                for (QuestionAnswerDto questionAnswer : questionAnswerList) {
                    // 그 중에 주관식 답변만
                    if (questionAnswer.getQuestionType() == 0) {
                        ChoiceDetailDto choiceDto = new ChoiceDetailDto();
                        choiceDto.setId(questionAnswer.getId());
                        choiceDto.setTitle(questionAnswer.getCheckAnswer());
                        choiceDto.setCount(0);

                        choiceDtos.add(choiceDto);
                    }
                }
            } else {
                for (Choice choice : questionDocument.getChoiceList()) {
                    ChoiceDetailDto choiceDto = new ChoiceDetailDto();
                    choiceDto.setId(choice.getId());
                    choiceDto.setTitle(choice.getTitle());
                    choiceDto.setCount(choice.getCount());

                    choiceDtos.add(choiceDto);
                }
            }
            questionDto.setChoiceList(choiceDtos);

            List<WordCloudDto> wordCloudDtos = new ArrayList<>();
            for (WordCloud wordCloud : questionDocument.getWordCloudList()) {
                WordCloudDto wordCloudDto = new WordCloudDto();
                wordCloudDto.setId(wordCloud.getId());
                wordCloudDto.setTitle(wordCloud.getTitle());
                wordCloudDto.setCount(wordCloud.getCount());

                wordCloudDtos.add(wordCloudDto);
            }
            questionDto.setWordCloudDtos(wordCloudDtos);

            questionDtos.add(questionDto);
        }
        surveyDetailDto.setQuestionList(questionDtos);

        log.info(String.valueOf(surveyDetailDto));
        return surveyDetailDto;
    }

    public Choice getChoice(Long id) {
        return choiceRepository.findById(id).orElse(null);
    }

    public QuestionDocument getQuestion(Long id) {
        return questionDocumentRepository.findById(id).orElse(null);
    }

    public QuestionDocument getQuestionByChoiceId(Long id) {
        return choiceRepository.findById(id).map(Choice::getQuestionDocument).orElse(null);
    }

    @Transactional
    public void setWordCloud(Long questionId, List<WordCloudDto> wordCloudDtos) {
        List<WordCloud> wordCloudList = new ArrayList<>();
        for (WordCloudDto wordCloudDto : wordCloudDtos) {
            WordCloud wordCloud = new WordCloud();
            wordCloud.setId(wordCloudDto.getId());
            wordCloud.setTitle(wordCloudDto.getTitle());
            wordCloud.setCount(wordCloudDto.getCount());
            wordCloud.setQuestionDocument(questionDocumentRepository.findById(questionId).get());
            wordCloudList.add(wordCloud);
        }
        wordCloudRepository.deleteAllByQuestionDocument(questionDocumentRepository.findById(questionId).get());
        questionDocumentRepository.findById(questionId).get().setWordCloudList(wordCloudList);
        questionDocumentRepository.flush();
    }

    @Transactional
    public void updateSurvey(SurveyRequestDto requestDto, Long surveyId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findById(surveyId).get();
        surveyDocument.updateSurvey(requestDto);
    }

    @Transactional
    public void deleteSurvey(Long id) {
        surveyDocumentRepository.deleteById(id);
    }

    @Transactional
    public void managementDate(Long id, DateDto request) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findById(id).get();
        surveyDocument.setDate(DateManagement.builder()
                .startDate(request.getStartDate())
                .deadline(request.getEndDate())
                .build());
    }

    @Transactional
    public void managementEnable(Long id, Boolean enable) {
        Optional<SurveyDocument> surveyDocument = surveyDocumentRepository.findById(id);
        surveyDocument.ifPresent(document -> document.getDate().setIsEnabled(enable));
    }

    public ManagementResponseDto managementSurvey(Long id) {
        return surveyDocumentRepository.findById(id).map(document -> ManagementResponseDto.builder()
                .startDate(document.getDate().getStartDate())
                .endDate(document.getDate().getDeadline())
                .enable(document.getDate().getIsEnabled())
                .build()).orElse(null);
    }
}
