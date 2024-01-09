package com.example.surveydocument.survey.service;

import com.example.surveydocument.restAPI.service.RestApiService;
import com.example.surveydocument.survey.domain.*;
import com.example.surveydocument.survey.repository.choice.ChoiceRepository;
import com.example.surveydocument.survey.repository.date.DateRepository;
import com.example.surveydocument.survey.repository.design.DesignRepository;
import com.example.surveydocument.survey.repository.questionDocument.QuestionDocumentRepository;
import com.example.surveydocument.survey.repository.surveyDocument.SurveyDocumentRepository;
import com.example.surveydocument.survey.request.*;
import com.example.surveydocument.survey.response.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyDocumentService {
    private final SurveyDocumentRepository surveyDocumentRepository;
    private final DesignRepository designRepository;
    private final QuestionDocumentRepository questionDocumentRepository;
    private final ChoiceRepository choiceRepository;
    private final DateRepository dateRepository;
    private final RestApiService apiService;

    @Transactional
    public Long createSurvey(HttpServletRequest request, SurveyRequestDto surveyRequest) {
        // 유저 정보 받아오기
        Long userId = apiService.getCurrentUserFromUser(request);

        // Survey Request 를 Survey Document 에 저장하기
        SurveyDocument surveyDocument = SurveyDocument.builder()
                .userId(userId)
                .title(surveyRequest.getTitle())
                .description(surveyRequest.getDescription())
                .type(surveyRequest.getType())
                .reliability(surveyRequest.getReliability()) // 진정성 검사
                .countAnswer(0)
                .build();
        surveyDocumentRepository.save(surveyDocument);

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

            questionDocumentRepository.save(questionDocument);
            // 주관식
            if (questionRequestDto.getType() == 0) continue;
            // 객관식, 찬부식일 경우 선지 저장
            for (ChoiceRequestDto choiceRequestDto : questionRequestDto.getChoiceList()) {
                Choice choice = Choice.builder()
                        .questionDocument(questionDocument)
                        .title(choiceRequestDto.getChoiceName())
                        .count(0)
                        .build();
                choiceRepository.save(choice);
            }
        }
        SurveyDocument save = surveyDocumentRepository.save(surveyDocument);

        return save.getId();
    }

    // list method 로 SurveyDocument 조회
    public Page<SurveyPageDto> readSurveyList(HttpServletRequest request1, PageRequestDto request2) {
        Long userCode = apiService.getCurrentUserFromUser(request1);
        PageRequest pageRequest = PageRequest.of(request2.getPage(), 10);

        return surveyDocumentRepository.pagingSurvey(userCode, request2.getSort1(), request2.getSort2(), pageRequest);
    }

    public SurveyDocument getSurveyDocument(Long surveyDocumentId) {
        return surveyDocumentRepository.findById(surveyDocumentId)
                .orElseThrow(() -> new RuntimeException("No SurveyDocument found with ID: " + surveyDocumentId));
    }

    @Transactional
    public void countChoice(Long choiceId) {
        choiceRepository.findById(choiceId)
                .orElseThrow(() -> new RuntimeException("No Choice found with ID: " + choiceId))
                .addCount();
    }

    // 설문 응답자 수 + 1
    @Transactional
    public void countSurveyDocument(Long surveyDocumentId) {
        surveyDocumentRepository.findById(surveyDocumentId)
                .orElseThrow(() -> new RuntimeException("No Choice found with ID: " + surveyDocumentId))
                .addCountAnswer();
    }

    // SurveyDocument Response 보낼 SurveyDetailDto로 변환하는 메서드
    public SurveyDetailDto readSurveyDetail(Long surveyDocumentId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findSurveyById(surveyDocumentId)
                .orElseThrow(() -> new RuntimeException("No surveyDocument found with ID: " + surveyDocumentId));
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

    public ChoiceDetailDto getChoice(Long id) {
        Choice choice = choiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No Choice found with ID: " + id));
        ChoiceDetailDto choiceDetailDto = new ChoiceDetailDto();
        choiceDetailDto.setId(choice.getId());
        choiceDetailDto.setTitle(choice.getTitle());
        choiceDetailDto.setCount(choice.getCount());
        return choiceDetailDto;
    }

    public QuestionDetailDto getQuestion(Long id) {
        return getQuestionDto(
                questionDocumentRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("No Choice found with ID: " + id)));
    }

    public QuestionDetailDto getQuestionByChoiceId(Long id) {
        return getQuestionDto(
                choiceRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("No Choice found with ID: " + id))
                        .getQuestionDocument());
    }

    @Transactional
    public void updateSurvey(HttpServletRequest request, SurveyRequestDto requestDto, Long surveyId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findByIdToUpdate(surveyId)
                .orElseThrow(() -> new RuntimeException("No SurveyDocument found with ID: " + surveyId));
        Long userId = surveyDocument.getUserId();
        Long jwtUserId = apiService.getCurrentUserFromUser(request);
        if (Objects.equals(userId, jwtUserId)) {
            surveyDocument.updateSurvey(requestDto);
        }
    }

    @Transactional
    public void deleteSurvey(HttpServletRequest request, Long surveyId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findByIdToUpdate(surveyId)
                .orElseThrow(() -> new RuntimeException("No SurveyDocument found with ID: " + surveyId));
        Long userId = surveyDocument.getUserId();
        Long jwtUserId = apiService.getCurrentUserFromUser(request);
        if (Objects.equals(userId, jwtUserId)) {
            surveyDocumentRepository.deleteById(surveyId);

        }
    }

    @Transactional
    public void managementDate(Long id, DateDto request) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No SurveyDocument found with ID: " + id));
        surveyDocument.setDate(DateManagement.builder()
                .startDate(request.getStartDate())
                .deadline(request.getEndDate())
                .build());
    }

    @Transactional
    public void managementEnable(Long id, Boolean enable) {
        surveyDocumentRepository.updateManage(id, enable);
    }

    public ManagementResponseDto managementSurvey(Long id) {
        return surveyDocumentRepository.findManageById(id);
    }


    public SurveyDetailDto2 readSurveyDetail2(Long surveyDocumentId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findSurveyById(surveyDocumentId)
                .orElseThrow(() -> new RuntimeException("No SurveyDocument found with ID: " + surveyDocumentId));
        SurveyDetailDto2 surveyDetailDto = new SurveyDetailDto2();

        // SurveyDocument에서 SurveyDetailDto로 데이터 복사
        surveyDetailDto.setId(surveyDocument.getId());
        surveyDetailDto.setTitle(surveyDocument.getTitle());
        surveyDetailDto.setDescription(surveyDocument.getDescription());

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

    private QuestionDetailDto getQuestionDto(QuestionDocument questionDocument) {
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

        return questionDto;
    }
}
