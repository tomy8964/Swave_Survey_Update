package com.example.surveydocument.survey.service;

import com.example.surveydocument.exception.InvalidUserException;
import com.example.surveydocument.exception.NotFoundException;
import com.example.surveydocument.restAPI.service.RestApiService;
import com.example.surveydocument.survey.domain.QuestionDocument;
import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.repository.choice.ChoiceRepository;
import com.example.surveydocument.survey.repository.date.DateRepository;
import com.example.surveydocument.survey.repository.design.DesignRepository;
import com.example.surveydocument.survey.repository.questionDocument.QuestionDocumentRepository;
import com.example.surveydocument.survey.repository.surveyDocument.SurveyDocumentRepository;
import com.example.surveydocument.survey.request.DateDto;
import com.example.surveydocument.survey.request.PageRequestDto;
import com.example.surveydocument.survey.request.SurveyRequestDto;
import com.example.surveydocument.survey.response.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyDocumentServiceImpl implements SurveyDocumentService {
    private final SurveyDocumentRepository surveyDocumentRepository;
    private final DesignRepository designRepository;
    private final QuestionDocumentRepository questionDocumentRepository;
    private final ChoiceRepository choiceRepository;
    private final DateRepository dateRepository;
    private final RestApiService apiService;
    private final TranslationService translationService;

    /**
     * 설문 생성
     * <p>
     * 설문 생성 시 설문, 디자인, 날짜, 문항, 선택지를 저장
     *
     * @param request       사용자 정보가 담긴 HttpServletRequest
     * @param surveyRequest 설문 생성 정보가 담긴 Request
     * @return 생성된 설문 ID
     * <p>
     * stream이 아니라 forEach를 사용한 이유 :
     * stream은 병렬처리를 하기 때문에 순서가 보장되지 않음
     * forEach는 순차적으로 처리하기 때문에 순서가 보장됨
     * 데이터 베이스에 저장할 때 순서가 보장되어야 하기 때문에 forEach를 사용
     */
    @Override
    @Transactional
    public Long createSurvey(HttpServletRequest request, SurveyRequestDto surveyRequest) {
        Long userId = getCurrentUserId(request);
        SurveyDocument surveyDocument = surveyDocumentRepository.save(translationService.DtoToEntity(surveyRequest, userId));

        designRepository.save(translationService.DtoToEntity(surveyRequest.getDesign(), surveyDocument));
        dateRepository.save(translationService.DtoToEntity(surveyRequest.getDate(), surveyDocument));

        surveyRequest.getQuestionRequest().forEach(questionRequestDto -> {
            QuestionDocument questionDocument = translationService.DtoToEntity(questionRequestDto, surveyDocument);
            questionDocumentRepository.save(questionDocument);
            if (questionRequestDto.getType() == 0) return;
            questionRequestDto.getChoiceList().forEach(choiceRequestDto -> choiceRepository.save(translationService.DtoToEntity(choiceRequestDto, questionDocument)));
        });

        return surveyDocumentRepository.save(surveyDocument).getId();
    }

    /**
     * 설문 수정
     * <p>
     * 설문 수정을 요청 하는 유저가 수정할 권한이 있는지 ID를 통해 확인
     *
     * @param request    설문 수정을 요청 하는 유저 정보
     * @param requestDto 설문 수정 정보
     * @param surveyId   수정할 설문 ID
     * @return 수정 완료된 설문 ID
     * @throws NotFoundException    수정할 설문이 없을 경우
     * @throws InvalidUserException 수정할 권한이 없을 경우
     */
    @Override
    @Transactional
    public Long updateSurvey(HttpServletRequest request, SurveyRequestDto requestDto, Long surveyId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findSurveyById(surveyId)
                .orElseThrow(() -> new NotFoundException("설문"));
        if (Objects.equals(surveyDocument.getUserId(), getCurrentUserId(request))) {
            surveyDocumentRepository.deleteById(surveyId);
            return createSurvey(request, requestDto);
        } else throw new InvalidUserException("이 설문을 수정할 권한이 없습니다.");
    }

    /**
     * 설문 페이지네이션 조회
     *
     * @param request1 사용자 정보가 담긴 HttpServletRequest
     * @param request2 페이지네이션 정보가 담긴 Request
     * @return Page 조회 후 반환
     */
    @Override
    public Page<SurveyPageDto> readSurveyList(HttpServletRequest request1, PageRequestDto request2) {
        return surveyDocumentRepository.pagingSurvey(
                getCurrentUserId(request1),
                request2.getSort1(),
                request2.getSort2(),
                PageRequest.of(request2.getPage(), 10)
        );
    }

    /**
     * 선택지 카운트 증가 메서드
     *
     * @param choiceId 선택지 ID
     * @return 선택지 ID
     * @throws NotFoundException 선택지가 없을 경우
     */
    @Override
    @Transactional
    public Long countChoice(Long choiceId) {
        choiceRepository.findById(choiceId)
                .orElseThrow(() -> new NotFoundException("선택지"))
                .addCount();
        return choiceId;
    }

    /**
     * 설문 응답자 수 1 증가 메서드
     *
     * @param surveyDocumentId 설문 응답자 수 증가 시킬 설문의 ID
     * @return 응답자 수 증가된 설문의 ID 재반환
     * @throws NotFoundException 설문이 없을 경우
     */
    @Override
    @Transactional
    public Long countSurveyDocument(Long surveyDocumentId) {
        surveyDocumentRepository.findById(surveyDocumentId)
                .orElseThrow(() -> new NotFoundException("설문"))
                .addCountAnswer();
        return surveyDocumentId;
    }

    /**
     * 설문 상세 조회
     *
     * @param surveyDocumentId 설문 ID
     * @return 설문 상세 조회 결과
     * @throws NotFoundException 설문이 없을 경우
     */
    @Override
    public SurveyDetailDto readSurveyDetail(Long surveyDocumentId) {
        return translationService.entityToDto1(
                surveyDocumentRepository.findSurveyById(surveyDocumentId)
                        .orElseThrow(() -> new NotFoundException("설문")));
    }

    /**
     * 선택지 상세 조회
     *
     * @param choiceId 선택지 ID
     * @return 선택지 상세 조회 결과
     * @throws NotFoundException 선택지가 없을 경우
     */
    @Override
    public ChoiceDetailDto getChoice(Long choiceId) {
        return translationService.entityToDto(
                choiceRepository.findById(choiceId)
                        .orElseThrow(() -> new NotFoundException("선택지")));
    }

    /**
     * 문항 상세 조회
     *
     * @param questionId 문항 ID
     * @return 문항 상세 조회 결과
     * @throws NotFoundException 문항이 없을 경우
     */
    @Override
    public QuestionDetailDto getQuestion(Long questionId) {
        return translationService.entityToDto(
                questionDocumentRepository.findById(questionId)
                        .orElseThrow(() -> new NotFoundException("문항")));
    }

    /**
     * 선택지로 문항 조회
     *
     * @param choiceId 선택지 ID
     * @return 선택지로 조회된 문항 상세 조회 결과
     * @throws NotFoundException 문항이 없을 경우
     */
    @Override
    public QuestionDetailDto getQuestionByChoiceId(Long choiceId) {
        return translationService.entityToDto(
                choiceRepository.findById(choiceId)
                        .orElseThrow(() -> new NotFoundException("문항"))
                        .getQuestionDocument());
    }


    /**
     * 설문 삭제
     * <p>
     * 설문 삭제를 요청 하는 유저가 삭제할 권한이 있는지 ID를 통해 확인
     *
     * @param request  설문 삭제을 요청 하는 유저 정보
     * @param surveyId 삭제할 설문 ID
     * @return 삭제 완료된 설문 ID
     * @throws NotFoundException    삭제할 설문이 없을 경우
     * @throws InvalidUserException 삭제할 권한이 없을 경우
     */
    @Override
    @Transactional
    public Long deleteSurvey(HttpServletRequest request, Long surveyId) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findSurveyById(surveyId)
                .orElseThrow(() -> new NotFoundException("설문"));
        if (!Objects.equals(surveyDocument.getUserId(), getCurrentUserId(request)))
            throw new InvalidUserException("이 설문을 삭제할 권한이 없습니다.");

        surveyDocumentRepository.deleteById(surveyId);
        return surveyId;
    }

    /**
     * 설문 날짜 수정
     *
     * @param surveyDocumentId 설문 ID
     * @param request          날짜 수정 정보
     * @return 수정된 설문 ID
     * @throws NotFoundException 설문이 없을 경우
     */
    @Override
    @Transactional
    public Long updateDate(Long surveyDocumentId, DateDto request) {
        SurveyDocument surveyDocument = surveyDocumentRepository.findById(surveyDocumentId)
                .orElseThrow(() -> new NotFoundException("설문"));
        surveyDocument.setDate(translationService.DtoToEntity(request, surveyDocument));
        return surveyDocumentId;
    }

    /**
     * 설문 활성화/비활성화 관리
     *
     * @param id     설문 ID
     * @param enable 활성화 여부
     * @return 활성화 여부
     */
    @Override
    @Transactional
    public Boolean managementEnable(Long id, Boolean enable) {
        return surveyDocumentRepository.updateManage(id, enable);
    }

    /**
     * 설문 관리 조회
     *
     * @param id 설문 ID
     * @return 설문 관리 조회 결과
     * @throws NotFoundException 설문이 없을 경우
     */
    @Override
    public ManagementResponseDto managementSurvey(Long id) {
        return surveyDocumentRepository.findManageById(id)
                .orElseThrow(() -> new NotFoundException("설문"));
    }

    /**
     * 설문 상세 조회
     *
     * @param surveyDocumentId 설문 ID
     * @return 설문 상세 조회 결과
     * @throws NotFoundException 설문이 없을 경우
     */
    @Override
    public SurveyDetailDto2 readSurveyDetail2(Long surveyDocumentId) {
        return translationService.entityToDto2(surveyDocumentRepository.findSurveyById(surveyDocumentId)
                .orElseThrow(() -> new NotFoundException("설문")));
    }

    /**
     * 현재 유저 조회
     *
     * @param request 사용자 정보가 담긴 HttpServletRequest
     * @return 현재 유저 ID
     * @throws InvalidUserException 유저가 없을 경우
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        return apiService.getCurrentUserFromJWTToken(request)
                .orElseThrow(InvalidUserException::new);
    }
}
