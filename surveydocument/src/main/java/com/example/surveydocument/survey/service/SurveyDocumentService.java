package com.example.surveydocument.survey.service;

import com.example.surveydocument.exception.InvalidUserException;
import com.example.surveydocument.exception.NotFoundException;
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
        Long userId = getCurrentUserId(request);

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
        Long userCode = getCurrentUserId(request1);
        PageRequest pageRequest = PageRequest.of(request2.getPage(), 10);

        return surveyDocumentRepository.pagingSurvey(userCode, request2.getSort1(), request2.getSort2(), pageRequest);
    }

    @Transactional
    public Long countChoice(Long choiceId) {
        choiceRepository.findById(choiceId)
                .orElseThrow(() -> new NotFoundException("선택지"))
                .addCount();
        return choiceId;
    }

    // 설문 응답자 수 + 1
    @Transactional
    public Long countSurveyDocument(Long surveyDocumentId) {
        surveyDocumentRepository.findById(surveyDocumentId)
                .orElseThrow(() -> new NotFoundException("설문"))
                .addCountAnswer();
        return surveyDocumentId;
    }

    // SurveyDocument Response 보낼 SurveyDetailDto로 변환하는 메서드
    public SurveyDetailDto readSurveyDetail(Long surveyDocumentId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findSurveyById(surveyDocumentId)
                .orElseThrow(() -> new NotFoundException("설문"));
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

    public ChoiceDetailDto getChoice(Long choiceId) {
        return ChoiceDetailDto.fromChoice(choiceRepository.findById(choiceId)
                .orElseThrow(() -> new NotFoundException("선택지")));
    }

    public QuestionDetailDto getQuestion(Long questionId) {
        return getQuestionDto(
                questionDocumentRepository.findById(questionId)
                        .orElseThrow(() -> new NotFoundException("문항")));
    }

    public QuestionDetailDto getQuestionByChoiceId(Long choiceId) {
        return getQuestionDto(
                choiceRepository.findById(choiceId)
                        .orElseThrow(() -> new NotFoundException("문항"))
                        .getQuestionDocument());
    }

    @Transactional
    public Long updateSurvey(HttpServletRequest request, SurveyRequestDto requestDto, Long surveyId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findSurveyById(surveyId)
                .orElseThrow(() -> new NotFoundException("설문"));
        Long userId = surveyDocument.getUserId();
        Long jwtUserId = getCurrentUserId(request);
        if (Objects.equals(userId, jwtUserId)) {
            return surveyDocument.updateSurvey(requestDto);
        } else throw new InvalidUserException("이 설문을 수정할 권한이 없습니다.");
    }

    @Transactional
    public Long deleteSurvey(HttpServletRequest request, Long surveyId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findById(surveyId)
                .orElseThrow(() -> new NotFoundException("설문"));
        Long userId = surveyDocument.getUserId();
        Long jwtUserId = getCurrentUserId(request);
        if (Objects.equals(userId, jwtUserId)) {
            surveyDocumentRepository.deleteById(surveyId);
            return surveyId;
        } else throw new InvalidUserException("이 설문을 삭제할 권한이 없습니다.");
    }

    @Transactional
    public Long managementDate(Long id, DateDto request) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("설문"));
        surveyDocument.setDate(DateManagement.builder()
                .startDate(request.getStartDate())
                .deadline(request.getEndDate())
                .build());
        return id;
    }

    @Transactional
    public Boolean managementEnable(Long id, Boolean enable) {
        surveyDocumentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("설문"));
        return surveyDocumentRepository.updateManage(id, enable);
    }

    public ManagementResponseDto managementSurvey(Long id) {
        return surveyDocumentRepository.findManageById(id)
                .orElseThrow(() -> new NotFoundException("설문"));
    }


    public SurveyDetailDto2 readSurveyDetail2(Long surveyDocumentId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findSurveyById(surveyDocumentId)
                .orElseThrow(() -> new NotFoundException("설문"));
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
                        choiceDtos.add(ChoiceDetailDto.fromChoice(Choice.builder()
                                .questionDocument(questionDocument)
                                .title(questionAnswer.getCheckAnswer())
                                .count(0)
                                .build()));
                    }
                }
            } else {
                for (Choice choice : questionDocument.getChoiceList()) {
                    choiceDtos.add(ChoiceDetailDto.fromChoice(choice));
                }
            }
            questionDto.setChoiceList(choiceDtos);

            List<WordCloudDto> wordCloudDtos = new ArrayList<>();
            for (WordCloud wordCloud : questionDocument.getWordCloudList()) {
                wordCloudDtos.add(WordCloudDto.fromWordCloud(wordCloud));
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
        // 주관식이면 choice title에 주관식 응답을 저장
        // 객관식 찬부식 -> 기존 방식 과 똑같이 count를 올려서 저장
        List<ChoiceDetailDto> choiceDtos = new ArrayList<>();
        if (questionDocument.getQuestionType() == 0) {
            // 주관식 답변들 리스트
            // REST API GET questionAnswersByCheckAnswerId
            List<QuestionAnswerDto> questionAnswerList = apiService.getQuestionAnswersByCheckAnswerId(questionDocument.getId());
            for (QuestionAnswerDto questionAnswer : questionAnswerList) {
                // 그 중에 주관식 답변만
                if (questionAnswer.getQuestionType() == 0) {
                    choiceDtos.add(ChoiceDetailDto.fromChoice(Choice.builder()
                            .questionDocument(questionDocument)
                            .title(questionAnswer.getCheckAnswer())
                            .count(0)
                            .build()));
                }
            }
        } else {
            for (Choice choice : questionDocument.getChoiceList()) {
                choiceDtos.add(ChoiceDetailDto.fromChoice(choice));
            }
        }
        questionDto.setChoiceList(choiceDtos);

        List<WordCloudDto> wordCloudDtos = new ArrayList<>();
        for (WordCloud wordCloud : questionDocument.getWordCloudList()) {
            wordCloudDtos.add(WordCloudDto.fromWordCloud(wordCloud));
        }
        questionDto.setWordCloudDtos(wordCloudDtos);

        return questionDto;
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        return apiService.getCurrentUserFromJWTToken(request)
                .orElseThrow(InvalidUserException::new);
    }
}
