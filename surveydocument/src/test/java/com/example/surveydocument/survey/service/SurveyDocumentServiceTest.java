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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.example.surveydocument.survey.controller.SurveyDocumentExternalControllerTest.createSurveyRequestDto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class SurveyDocumentServiceTest {

    @Mock
    private SurveyDocumentRepository surveyDocumentRepository;
    @Mock
    private DesignRepository designRepository;
    @Mock
    private QuestionDocumentRepository questionDocumentRepository;
    @Mock
    private ChoiceRepository choiceRepository;
    @Mock
    private DateRepository dateRepository;
    @Mock
    private RestApiService apiService;
    @Mock
    private TranslationService translationService;

    @InjectMocks
    private SurveyDocumentServiceImpl surveyDocumentService;

    public static SurveyDocument createSurveyDocument() {
        SurveyDocument surveyDocument = SurveyDocument.builder()
                .id(1L)
                .userId(1L)
                .title("설문 제목")
                .description("설문 내용")
                .type(0)
                .acceptResponse(true)
                .reliability(true)
                .build();

        Design.builder()
                .surveyDocument(surveyDocument)
                .id(1L)
                .font("폰트")
                .fontSize(1)
                .backColor("검은색").build();

        DateManagement.builder()
                .surveyDocument(surveyDocument)
                .id(1L)
                .startDate(new Date())
                .deadline(new Date())
                .isEnabled(true)
                .build();

        QuestionDocument questionDocument1 = QuestionDocument.builder()
                .surveyDocument(surveyDocument)
                .id(1L)
                .title("객관식 설문 문항")
                .questionType(1)
                .build();

        Choice.builder()
                .questionDocument(questionDocument1)
                .id(1L)
                .title("객관식 1번")
                .build();

        Choice.builder()
                .questionDocument(questionDocument1)
                .id(2L)
                .title("객관식 2번")
                .build();

        QuestionDocument questionDocument2 = QuestionDocument.builder()
                .surveyDocument(surveyDocument)
                .id(2L)
                .title("주관식 설문 문항")
                .questionType(0)
                .build();

        WordCloud.builder()
                .questionDocument(questionDocument2)
                .id(1L)
                .title("주관식 답변입니다.")
                .count(1)
                .build();

        WordCloud.builder()
                .questionDocument(null)
                .id(1L)
                .title("주관식 답변입니다.")
                .count(1)
                .build();

        Design.builder()
                .surveyDocument(null)
                .id(1L)
                .font("폰트")
                .fontSize(1)
                .backColor("검은색").build();

        return surveyDocument;
    }

    public static PageRequestDto createPageRequestDto() {
        return new PageRequestDto("grid", 0, "date", "ascending");
    }

    public static Page<SurveyPageDto> createExpectedPage() {
        List<SurveyPageDto> content = new ArrayList<>();

        // 총 11개(1일부터 11일까지)의 설문 리스트 생성
        for (int number = 1; number < 11; number++) {
            SurveyPageDto surveyPageDto = new SurveyPageDto("test" + number,
                    new Date());
            content.add(surveyPageDto);
        }

        return new PageImpl<>(content, PageRequest.of(0, 10), content.size());
    }

    @Test
    @DisplayName("설문 생성 성공")
    public void createSurveySuccess() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        SurveyDocument surveyDocument = createSurveyDocument();

        Choice choice1 = Choice.builder()
                .id(1L)
                .build();

        when(apiService.getCurrentUserFromJWTToken(request)).thenReturn(Optional.of(1L));

        when(translationService.DtoToEntity(any(SurveyRequestDto.class), any())).thenReturn(surveyDocument);
        when(surveyDocumentRepository.save(any())).thenReturn(surveyDocument);

        when(translationService.DtoToEntity(any(DesignRequestDto.class), any(SurveyDocument.class))).thenReturn(surveyDocument.getDesign());
        when(designRepository.save(any(Design.class))).thenReturn(surveyDocument.getDesign());

        when(translationService.DtoToEntity(any(DateDto.class), any(SurveyDocument.class))).thenReturn(surveyDocument.getDate());
        when(dateRepository.save(any(DateManagement.class))).thenReturn(surveyDocument.getDate());

        when(translationService.DtoToEntity(any(QuestionRequestDto.class), any(SurveyDocument.class))).thenReturn(surveyDocument.getQuestionDocumentList().get(1));
        when(translationService.DtoToEntity(any(ChoiceRequestDto.class), any(QuestionDocument.class))).thenReturn(choice1);

        when(questionDocumentRepository.save(any())).thenReturn(surveyDocument.getQuestionDocumentList().get(0));
        when(questionDocumentRepository.save(any())).thenReturn(surveyDocument.getQuestionDocumentList().get(1));

        // when
        Long surveyId = surveyDocumentService.createSurvey(request, surveyRequestDto);

        // then
        assertEquals(1L, surveyId);
    }

    @Test
    @DisplayName("설문 생성 실패 - 올바르지 않은 JWT 토큰")
    public void createSurveyFail() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();

        when(apiService.getCurrentUserFromJWTToken(request)).thenReturn(Optional.empty());

        // when & then
        assertThrows(InvalidUserException.class, () -> surveyDocumentService.createSurvey(request, surveyRequestDto));
    }

    @Test
    @DisplayName("설문 목록 조회 성공")
    public void readSurveyListSuccess() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        PageRequestDto pageRequestDto = createPageRequestDto();

        when(apiService.getCurrentUserFromJWTToken(request)).thenReturn(Optional.of(1L));
        when(surveyDocumentRepository.pagingSurvey(any(), any(), any(), any())).thenReturn(createExpectedPage());

        // when
        Page<SurveyPageDto> result = surveyDocumentService.readSurveyList(request, pageRequestDto);

        // then
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getTotalElements());

        // 내용 검증
        List<SurveyPageDto> content = result.getContent();
        assertEquals(10, content.size()); // 페이지 당 10개의 데이터가 있는지 확인
        for (int i = 0; i < 10; i++) {
            SurveyPageDto surveyPageDto = content.get(i);
            assertEquals("test" + (i + 1), surveyPageDto.getTitle()); // 제목 검증
            assertNotNull(surveyPageDto.getStartDate()); // 날짜가 null이 아닌지 확인
        }
    }

    @Test
    @DisplayName("설문 목록 조회 실패 - 올바르지 않은 유저 정보")
    public void readSurveyListFail() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        PageRequestDto pageRequestDto = createPageRequestDto();

        when(apiService.getCurrentUserFromJWTToken(request)).thenReturn(Optional.empty());

        // when & then
        assertThrows(InvalidUserException.class, () -> surveyDocumentService.readSurveyList(request, pageRequestDto));
    }

    @Test
    @DisplayName("설문 객관식 문항의 선택지 응답자 수 추가 성공")
    public void countChoiceSuccess() {
        // given
        Long choiceId = 1L;
        Choice choice = Choice.builder()
                .id(1L)
                .build();
        when(choiceRepository.findById(any())).thenReturn(Optional.of(choice));

        // when
        Long countedChoiceId = surveyDocumentService.countChoice(choiceId);

        // then
        assertEquals(choiceId, countedChoiceId);
    }

    @Test
    @DisplayName("설문 객관식 문항의 선택지 응답자 수 추가 실패 - 존재하지 않는 선택지")
    public void countChoiceFail() {
        // given
        Long choiceId = 1L;
        when(choiceRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> surveyDocumentService.countChoice(choiceId));
    }

    @Test
    @DisplayName("설문의 응답자 수 추가 성공")
    public void countSurveyDocumentSuccess() {
        // given
        Long surveyDocumentId = 1L;
        SurveyDocument surveyDocument = SurveyDocument.builder()
                .id(1L)
                .build();
        when(surveyDocumentRepository.findById(any())).thenReturn(Optional.of(surveyDocument));

        // when
        Long countedSurveyDocumentId = surveyDocumentService.countSurveyDocument(surveyDocumentId);

        // then
        assertEquals(surveyDocumentId, countedSurveyDocumentId);
    }

    @Test
    @DisplayName("설문의 응답자 수 추가 실패 - 존재하지 않는 설문")
    public void countSurveyDocumentFail() {
        // given
        Long surveyDocumentId = 1L;
        when(surveyDocumentRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> surveyDocumentService.countSurveyDocument(surveyDocumentId));
    }

    @Test
    @DisplayName("설문 응답 서비스로 보낼 DTO로 변환 성공")
    public void readSurveyDetailSuccess() {
        // given
        Long surveyDocumentId = 1L;
        SurveyDetailDto surveyDetailDto1 = SurveyDetailDto.builder()
                .id(1L)
                .title("설문 제목")
                .description("설문 내용")
                .reliability(true)
                .countAnswer(1)
                .build();
        SurveyDocument surveyDocument = createSurveyDocument();
        List<QuestionAnswerDto> questionAnswerDtos = new ArrayList<>();
        QuestionAnswerDto questionAnswerDto1 = QuestionAnswerDto.builder()
                .id(1L)
                .checkAnswer("주관식 답변입니다.")
                .questionType(0)
                .build();
        QuestionAnswerDto questionAnswerDto2 = QuestionAnswerDto.builder()
                .id(1L)
                .checkAnswer("객관식 답변입니다.")
                .questionType(2)
                .build();
        questionAnswerDtos.add(questionAnswerDto1);
        questionAnswerDtos.add(questionAnswerDto2);

        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.of(surveyDocument));
        when(translationService.entityToDto1(any(SurveyDocument.class))).thenReturn(surveyDetailDto1);

        // when
        SurveyDetailDto surveyDetailDto = surveyDocumentService.readSurveyDetail(surveyDocumentId);

        // then
        assertEquals(surveyDocumentId, surveyDetailDto.getId());
        assertEquals(surveyDocument.getTitle(), surveyDetailDto.getTitle());
        assertEquals(surveyDocument.getDescription(), surveyDetailDto.getDescription());
        assertEquals(surveyDocument.getReliability(), surveyDetailDto.getReliability());
    }

    @Test
    @DisplayName("설문 응답 서비스로 보낼 DTO로 변환 실패 - 존재하지 않는 설문")
    public void readSurveyDetailFail() {
        // given
        Long surveyDocumentId = 1L;
        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> surveyDocumentService.readSurveyDetail(surveyDocumentId));
    }

    @Test
    @DisplayName("설문 객관식 문항의 선택지 조회 성공")
    public void getChoiceSuccess() {
        // given
        ChoiceDetailDto choiceDetailDto1 = ChoiceDetailDto.builder()
                .id(1L)
                .title("객관식 1번")
                .count(0)
                .build();
        Choice choice = Choice.builder()
                .id(1L)
                .title("객관식 1번")
                .build();
        Long choiceId = 1L;
        when(choiceRepository.findById(any())).thenReturn(Optional.of(choice));
        when(translationService.entityToDto(any(Choice.class))).thenReturn(choiceDetailDto1);

        // when
        ChoiceDetailDto choiceDetailDto = surveyDocumentService.getChoice(choiceId);

        // then
        assertEquals(choice.getId(), choiceDetailDto.getId());
        assertEquals(choice.getCount(), choiceDetailDto.getCount());
        assertEquals(choice.getTitle(), choiceDetailDto.getTitle());
    }

    @Test
    @DisplayName("설문 객관식 문항의 선택지 조회 실패 - 존재하지 않는 선택지")
    public void getChoiceFail() {
        // given
        Long choiceId = 1L;
        when(choiceRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> surveyDocumentService.getChoice(choiceId));
    }

    @Test
    @DisplayName("설문 문항 조회 성공 - 객관식")
    public void getQuestionSuccess1() {
        // given
        QuestionDetailDto questionDetailDto1 = QuestionDetailDto.builder()
                .id(1L)
                .title("객관식 설문 문항")
                .questionType(1)
                .build();

        QuestionDocument questionDocument = QuestionDocument.builder()
                .id(1L)
                .title("객관식 설문 문항")
                .questionType(1)
                .build();

        Choice.builder()
                .questionDocument(questionDocument)
                .id(1L)
                .title("객관식 1번")
                .build();

        Choice.builder()
                .questionDocument(questionDocument)
                .id(2L)
                .title("객관식 2번")
                .build();
        Long questionDocumentId = 1L;

        when(questionDocumentRepository.findById(any())).thenReturn(Optional.of(questionDocument));
        when(translationService.entityToDto(any(QuestionDocument.class))).thenReturn(questionDetailDto1);

        // when
        QuestionDetailDto questionDetailDto = surveyDocumentService.getQuestion(questionDocumentId);

        // then
        assertEquals(questionDocument.getId(), questionDetailDto.getId());
        assertEquals(questionDocument.getQuestionType(), questionDetailDto.getQuestionType());
        assertEquals(questionDocument.getTitle(), questionDetailDto.getTitle());
    }

    @Test
    @DisplayName("설문 문항 조회 성공 - 주관식")
    public void getQuestionSuccess2() {
        // given
        QuestionDetailDto questionDetailDto1 = QuestionDetailDto.builder()
                .id(1L)
                .title("주관식 설문 문항")
                .questionType(0)
                .build();

        QuestionDocument questionDocument = QuestionDocument.builder()
                .id(2L)
                .title("주관식 설문 문항")
                .questionType(0)
                .build();

        WordCloud.builder()
                .questionDocument(questionDocument)
                .id(1L)
                .title("주관식 답변입니다.")
                .count(1)
                .build();
        Long questionDocumentId = 2L;
        List<QuestionAnswerDto> questionAnswerDtos = new ArrayList<>();
        QuestionAnswerDto questionAnswerDto1 = QuestionAnswerDto.builder()
                .id(1L)
                .checkAnswer("주관식 답변입니다.")
                .questionType(0)
                .build();
        QuestionAnswerDto questionAnswerDto2 = QuestionAnswerDto.builder()
                .id(1L)
                .checkAnswer("객관식 답변입니다.")
                .questionType(2)
                .build();
        questionAnswerDtos.add(questionAnswerDto1);
        questionAnswerDtos.add(questionAnswerDto2);

        when(questionDocumentRepository.findById(any())).thenReturn(Optional.of(questionDocument));
        when(translationService.entityToDto(any(QuestionDocument.class))).thenReturn(questionDetailDto1);

        // when
        QuestionDetailDto questionDetailDto = surveyDocumentService.getQuestion(questionDocumentId);

        // then
    }

    @Test
    @DisplayName("설문 문항 조회 실패 - 존재하지 않는 문항")
    public void getQuestionFail() {
        // given
        Long questionId = 1L;
        when(questionDocumentRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> surveyDocumentService.getQuestion(questionId));
    }

    @Test
    @DisplayName("선택지로 문항 조회 성공")
    public void getQuestionByChoiceIdSuccess() {
        // given
        QuestionDetailDto questionDetailDto1 = QuestionDetailDto.builder()
                .id(1L)
                .title("객관식 설문 문항")
                .questionType(1)
                .build();
        QuestionDocument questionDocument = QuestionDocument.builder()
                .id(1L)
                .title("객관식 설문 문항")
                .questionType(1)
                .build();

        Choice choice = Choice.builder()
                .id(1L)
                .title("객관식 1번")
                .questionDocument(questionDocument)
                .build();
        Long choiceId = 1L;
        when(choiceRepository.findById(any())).thenReturn(Optional.of(choice));
        when(translationService.entityToDto((any(QuestionDocument.class)))).thenReturn(questionDetailDto1);

        // when
        QuestionDetailDto questionDetailDto = surveyDocumentService.getQuestionByChoiceId(choiceId);

        // then
        assertEquals(questionDocument.getId(), questionDetailDto.getId());
        assertEquals(questionDocument.getQuestionType(), questionDetailDto.getQuestionType());
        assertEquals(questionDocument.getTitle(), questionDetailDto.getTitle());
    }

    @Test
    @DisplayName("선택지로 문항 조회 실패 - 존재하지 않는 선택지")
    public void getQuestionByChoiceIdFail() {
        // given
        Long choiceId = 1L;
        when(choiceRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> surveyDocumentService.getQuestionByChoiceId(choiceId));
    }

    @Test
    @DisplayName("설문 수정 성공")
    public void updateSurveySuccess() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        surveyRequestDto.setTitle("설문 수정 테스트");
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.of(surveyDocument));
        when(apiService.getCurrentUserFromJWTToken(any())).thenReturn(Optional.of(1L));

        Choice choice1 = Choice.builder()
                .id(1L)
                .build();

        when(apiService.getCurrentUserFromJWTToken(request)).thenReturn(Optional.of(1L));

        when(translationService.DtoToEntity(any(SurveyRequestDto.class), any())).thenReturn(surveyDocument);
        when(surveyDocumentRepository.save(any())).thenReturn(surveyDocument);

        when(translationService.DtoToEntity(any(DesignRequestDto.class), any(SurveyDocument.class))).thenReturn(surveyDocument.getDesign());
        when(designRepository.save(any(Design.class))).thenReturn(surveyDocument.getDesign());

        when(translationService.DtoToEntity(any(DateDto.class), any(SurveyDocument.class))).thenReturn(surveyDocument.getDate());
        when(dateRepository.save(any(DateManagement.class))).thenReturn(surveyDocument.getDate());

        when(translationService.DtoToEntity(any(QuestionRequestDto.class), any(SurveyDocument.class))).thenReturn(surveyDocument.getQuestionDocumentList().get(1));
        when(translationService.DtoToEntity(any(ChoiceRequestDto.class), any(QuestionDocument.class))).thenReturn(choice1);

        when(questionDocumentRepository.save(any())).thenReturn(surveyDocument.getQuestionDocumentList().get(0));
        when(questionDocumentRepository.save(any())).thenReturn(surveyDocument.getQuestionDocumentList().get(1));

        // when
        Long updatedSurveyId = surveyDocumentService.updateSurvey(request, surveyRequestDto, surveyDocument.getId());

        // then
        assertEquals(surveyDocument.getId(), updatedSurveyId);
    }

    @Test
    @DisplayName("설문 수정 실패 - 수정할 권한이 없는 유저")
    public void updateSurveyFail1() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.of(surveyDocument));
        when(apiService.getCurrentUserFromJWTToken(any())).thenReturn(Optional.of(2L));

        // when & then
        assertThrows(InvalidUserException.class, () -> surveyDocumentService.updateSurvey(request, surveyRequestDto, surveyDocument.getId()));
    }

    @Test
    @DisplayName("설문 수정 실패 - 올바르지 않은 유저")
    public void updateSurveyFail2() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.of(surveyDocument));
        when(apiService.getCurrentUserFromJWTToken(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(InvalidUserException.class, () -> surveyDocumentService.updateSurvey(request, surveyRequestDto, surveyDocument.getId()));
    }

    @Test
    @DisplayName("설문 수정 실패 - 존재하지 않는 설문")
    public void updateSurveyFail3() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> surveyDocumentService.updateSurvey(request, surveyRequestDto, surveyDocument.getId()));
    }

    @Test
    @DisplayName("설문 삭제 성공")
    public void deleteSurveySuccess() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        surveyRequestDto.setTitle("설문 삭제 테스트");
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.of(surveyDocument));
        when(apiService.getCurrentUserFromJWTToken(any())).thenReturn(Optional.of(1L));

        // when
        Long updatedSurveyId = surveyDocumentService.deleteSurvey(request, surveyDocument.getId());

        // then
        assertEquals(surveyDocument.getId(), updatedSurveyId);
    }

    @Test
    @DisplayName("설문 삭제 실패 - 삭제할 권한이 없는 유저")
    public void deleteSurveyFail1() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.of(surveyDocument));
        when(apiService.getCurrentUserFromJWTToken(any())).thenReturn(Optional.of(2L));

        // when & then
        assertThrows(InvalidUserException.class, () -> surveyDocumentService.deleteSurvey(request, surveyDocument.getId()));
    }

    @Test
    @DisplayName("설문 삭제 실패 - 올바르지 않은 유저")
    public void deleteSurveyFail2() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.of(surveyDocument));
        when(apiService.getCurrentUserFromJWTToken(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(InvalidUserException.class, () -> surveyDocumentService.deleteSurvey(request, surveyDocument.getId()));
    }

    @Test
    @DisplayName("설문 삭제 실패 - 존재하지 않는 설문")
    public void deleteSurveyFail3() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> surveyDocumentService.deleteSurvey(request, surveyDocument.getId()));
    }

    @Test
    @DisplayName("설문 날짜 관리 성공")
    public void managementDateSuccess() {
        // given
        DateDto date = DateDto.builder()
                .endDate(new Date())
                .startDate(new Date())
                .build();
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.findById(any())).thenReturn(Optional.of(surveyDocument));

        // when
        Long managementedSurveyId = surveyDocumentService.updateDate(surveyDocument.getId(), date);

        // then
        assertEquals(surveyDocument.getId(), managementedSurveyId);
    }

    @Test
    @DisplayName("설문 날짜 관리 실패 - 존재하지 않는 설문")
    public void managementDateFail() {
        // given
        DateDto date = DateDto.builder()
                .endDate(new Date())
                .startDate(new Date())
                .build();
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> surveyDocumentService.updateDate(surveyDocument.getId(), date));
    }

    @Test
    @DisplayName("설문 활성화/비활성화 성공")
    public void managementEnableSuccess() {
        // given
        Boolean enable = true;
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.updateManage(any(), any())).thenReturn(enable);

        // when
        Boolean result = surveyDocumentService.managementEnable(surveyDocument.getId(), enable);

        // then
        assertEquals(enable, result);
    }

    @Test
    @DisplayName("설문 관리 조회 성공")
    public void managementSurveySuccess() {
        // given
        SurveyDocument surveyDocument = createSurveyDocument();
        ManagementResponseDto manage = ManagementResponseDto.builder()
                .startDate(surveyDocument.getDate().getStartDate())
                .endDate(surveyDocument.getDate().getDeadline())
                .enable(surveyDocument.getDate().getIsEnabled())
                .build();
        when(surveyDocumentRepository.findManageById(any())).thenReturn(Optional.of(manage));

        // when
        ManagementResponseDto managementResponseDto = surveyDocumentService.managementSurvey(surveyDocument.getId());

        // then
        assertEquals(manage.getEnable(), managementResponseDto.getEnable());
        assertEquals(manage.getStartDate(), managementResponseDto.getStartDate());
        assertEquals(manage.getEndDate(), managementResponseDto.getEndDate());
    }

    @Test
    @DisplayName("설문 관리 조회 실패 - 존재하지 않는 설문")
    public void managementSurveyFail() {
        // given
        SurveyDocument surveyDocument = createSurveyDocument();
        when(surveyDocumentRepository.findManageById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> surveyDocumentService.managementSurvey(surveyDocument.getId()));
    }

    @Test
    @DisplayName("설문 분석 서비스로 보낼 DTO로 변환 성공")
    public void readSurveyDetail2Success() {
        // given
        Long surveyDocumentId = 1L;
        SurveyDocument surveyDocument = createSurveyDocument();
        SurveyDetailDto2 surveyDetailDto2 = SurveyDetailDto2.builder()
                .id(1L)
                .title("설문 제목")
                .description("설문 내용")
                .countAnswer(1)
                .build();
        List<QuestionAnswerDto> questionAnswerDtos = new ArrayList<>();
        QuestionAnswerDto questionAnswerDto1 = QuestionAnswerDto.builder()
                .id(1L)
                .checkAnswer("주관식 답변입니다.")
                .questionType(0)
                .build();
        QuestionAnswerDto questionAnswerDto2 = QuestionAnswerDto.builder()
                .id(1L)
                .checkAnswer("객관식 답변입니다.")
                .questionType(2)
                .build();
        questionAnswerDtos.add(questionAnswerDto1);
        questionAnswerDtos.add(questionAnswerDto2);

        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.of(surveyDocument));
        when(translationService.entityToDto2(any(SurveyDocument.class))).thenReturn(surveyDetailDto2);
        // when
        SurveyDetailDto2 surveyDetailDto = surveyDocumentService.readSurveyDetail2(surveyDocumentId);

        // then
        assertEquals(surveyDocumentId, surveyDetailDto.getId());
        assertEquals(surveyDocument.getTitle(), surveyDetailDto.getTitle());
        assertEquals(surveyDocument.getDescription(), surveyDetailDto.getDescription());
    }

    @Test
    @DisplayName("설문 분석 서비스로 보낼 DTO로 변환 실패 - 존재하지 않는 설문")
    public void readSurveyDetail2Fail() {
        // given
        Long surveyDocumentId = 1L;
        when(surveyDocumentRepository.findSurveyById(any())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> surveyDocumentService.readSurveyDetail2(surveyDocumentId));
    }
}