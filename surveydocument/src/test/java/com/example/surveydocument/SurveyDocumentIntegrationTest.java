package com.example.surveydocument;

import com.example.surveydocument.survey.request.SurveyRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static com.example.surveydocument.survey.controller.SurveyDocumentExternalControllerTest.createSurveyRequestDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SurveyDocumentIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    MockWebServer mockBackEnd;
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("설문 2개 생성 성공")
    public void createSurveySuccess() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        final Long FIRST_CREATED_SURVEY_ID = 1L;
        final Long SECOND_CREATED_SURVEY_ID = 2L;
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        mockBackEnd.enqueue(createMockResponse(userId));

        // when
        MvcResult createResult1 = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn();
        MvcResult createResult2 = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn();

        // then
        assertEquals(createResult1.getResponse().getContentAsString(), String.valueOf(FIRST_CREATED_SURVEY_ID));
        assertEquals(createResult2.getResponse().getContentAsString(), String.valueOf(SECOND_CREATED_SURVEY_ID));
    }

    private MockResponse createMockResponse(Object body) throws JsonProcessingException {
        return new MockResponse()
                .setBody(mapper.writeValueAsString(body))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }
//
//    /**
//     * 이 테스트는 설문 생성을 실패하는 경우를 검증합니다.
//     * 설문 생성시 설문 생성을 요청한 유저의 JWT 토큰이
//     * 올바르지 않거나 권한이 없는 경우입니다.
//     * 이 경우 Service에서 InvalidUserException을 발생시킵니다.
//     *
//     * @throws Exception JSON Parsing 예외를 던집니다.
//     * @RestControllerAdvice 에서 이를 감지해 에러를 핸들링합니다.
//     */
//    @Test
//    @DisplayName("설문 생성 실패 - 올바르지 않은 JWT 토큰")
//    public void createSurveyFail() throws Exception {
//        // given
//        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
//        when(surveyDocumentService.createSurvey(any(HttpServletRequest.class), any()))
//                .thenThrow(new InvalidUserException());
//
//        // when
//        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(surveyRequestDto))
//                )
//                .andExpect(status().isUnauthorized())
//                .andDo(print())
//                .andReturn();
//        // then
//        assertEquals(createResult.getResponse().getContentAsString(), "유저 정보가 올바르지 않습니다.");
//    }
//
//    @Test
//    @DisplayName("설문 목록 조회 성공")
//    public void readListSuccess() throws Exception {
//        // given
//        PageRequestDto pageRequestDto = createPageRequestDto();
//        Page<SurveyPageDto> expectedPage = createExpectedPage();
//        when(surveyDocumentService.readSurveyList(any(HttpServletRequest.class), any(PageRequestDto.class)))
//                .thenReturn(expectedPage);
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(post("/api/document/external/survey-list")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(pageRequestDto))
//                )
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andReturn();
//
//        String responseBody = mvcResult.getResponse().getContentAsString();
//        PageResponseDto<SurveyPageDto> actualPage = objectMapper.readValue(responseBody, new TypeReference<>() {
//        });
//        // then
//
//        assertEquals(actualPage.getContent().size(), expectedPage.getContent().size());
//        for (int i = 0; i < actualPage.getContent().size(); i++) {
//            assertEquals(actualPage.getContent().get(i).getTitle(), expectedPage.getContent().get(i).getTitle());
//            assertEquals(actualPage.getContent().get(i).getStartDate(), expectedPage.getContent().get(i).getStartDate());
//        }
//        assertEquals(actualPage.getNumber(), expectedPage.getNumber());
//        assertEquals(actualPage.getSize(), expectedPage.getSize());
//        assertEquals(actualPage.getTotalPages(), expectedPage.getTotalPages());
//        assertEquals(actualPage.getTotalElements(), expectedPage.getTotalElements());
//        assertEquals(actualPage.isLast(), expectedPage.isLast());
//        assertEquals(actualPage.getSort().isEmpty(), expectedPage.getSort().isEmpty());
//        assertEquals(actualPage.getSort().isSorted(), expectedPage.getSort().isSorted());
//        assertEquals(actualPage.getSort().isUnsorted(), expectedPage.getSort().isUnsorted());
//        assertEquals(actualPage.getNumberOfElements(), expectedPage.getNumberOfElements());
//        assertEquals(actualPage.isFirst(), expectedPage.isFirst());
//        assertEquals(actualPage.isEmpty(), expectedPage.isEmpty());
//        assertEquals(actualPage.getPageable().getSort().isEmpty(), expectedPage.getPageable().getSort().isEmpty());
//        assertEquals(actualPage.getPageable().getSort().isSorted(), expectedPage.getPageable().getSort().isSorted());
//        assertEquals(actualPage.getPageable().getSort().isUnsorted(), expectedPage.getPageable().getSort().isUnsorted());
//        assertEquals(actualPage.getPageable().getOffset(), expectedPage.getPageable().getOffset());
//        assertEquals(actualPage.getPageable().getPageNumber(), expectedPage.getPageable().getPageNumber());
//        assertEquals(actualPage.getPageable().getPageSize(), expectedPage.getPageable().getPageSize());
//        assertEquals(actualPage.getPageable().isPaged(), expectedPage.getPageable().isPaged());
//        assertEquals(actualPage.getPageable().isUnpaged(), expectedPage.getPageable().isUnpaged());
//    }
//
//    /**
//     * 설문 목록 조회시
//     * 설문 목록 조회를 요청한 유저의 JWT 토큰이
//     * 권한이 없는 경우 혹은 올바르지 않은 JWT 토큰일 경우입니다.
//     *
//     * @throws Exception
//     */
//    @Test
//    @DisplayName("설문 목록 조회 실패")
//    public void readListFail() throws Exception {
//        // given
//        PageRequestDto pageRequestDto = createPageRequestDto();
//        when(surveyDocumentService.readSurveyList(any(HttpServletRequest.class), any(PageRequestDto.class)))
//                .thenThrow(new InvalidUserException("올바르지 않은 유저입니다."));
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(post("/api/document/external/survey-list")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(pageRequestDto))
//                )
//                .andExpect(status().isUnauthorized())
//                .andDo(print())
//                .andReturn();
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "올바르지 않은 유저입니다.");
//    }
//
//    @Test
//    @DisplayName("설문 조회 성공")
//    public void readSurveySuccess() throws Exception {
//        // given
//        SurveyDetailDto expectedResponse = createSurveyDetailDto();
//        when(surveyDocumentService.readSurveyDetail(any(Long.class)))
//                .thenReturn(expectedResponse);
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(get("/api/document/external/survey-list/1"))
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andReturn();
//
//        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
//        SurveyDetailDto actualResponse = objectMapper.readValue(responseBody, new TypeReference<>() {
//        });
//
//        // then
//        assertEquals(actualResponse.getId(), expectedResponse.getId());
//        assertEquals(actualResponse.getTitle(), expectedResponse.getTitle());
//        assertEquals(actualResponse.getDescription(), expectedResponse.getDescription());
//        assertEquals(actualResponse.getCountAnswer(), expectedResponse.getCountAnswer());
//        assertEquals(actualResponse.getReliability(), expectedResponse.getReliability());
//        assertEquals(actualResponse.getStartDate(), expectedResponse.getStartDate());
//        assertEquals(actualResponse.getEndDate(), expectedResponse.getEndDate());
//        assertEquals(actualResponse.getEnable(), expectedResponse.getEnable());
//
//        assertEquals(actualResponse.getQuestionList().size(), expectedResponse.getQuestionList().size());
//    }
//
//    /**
//     * 설문 조회시
//     * 설문 조회를 요청한 설문이 존재하지 않는 경우
//     *
//     * @throws Exception
//     */
//    @Test
//    @DisplayName("설문 조회 실패 - 조회할 설문이 존재하지 않는 경우")
//    public void readSurveyFail() throws Exception {
//        // given
//        when(surveyDocumentService.readSurveyDetail(any(Long.class)))
//                .thenThrow(new NotFoundException("설문"));
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(get("/api/document/external/survey-list/1"))
//                .andExpect(status().isNotFound())
//                .andDo(print())
//                .andReturn();
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
//    }
//
//    @Test
//    @DisplayName("설문 수정 성공")
//    public void updateSurveySuccess() throws Exception {
//        // given
//        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
//        surveyRequestDto.setTitle("제목 수정");
//        when(surveyDocumentService.updateSurvey(any(HttpServletRequest.class), any(SurveyRequestDto.class), any(Long.class)))
//                .thenReturn(1L);
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(put("/api/document/external/update/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(surveyRequestDto))
//                )
//                .andExpect(status().isNoContent())
//                .andDo(print())
//                .andReturn();
//
//        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
//
//        // then
//        assertEquals(responseBody, "1");
//    }
//
//    /**
//     * 설문 수정시
//     * 설문 수정을 요청한 유저의 JWT 토큰이
//     * 올바르지 않은 JWT 토큰일 경우입니다.
//     *
//     * @throws Exception
//     */
//    @Test
//    @DisplayName("설문 수정 실패 - 올바르지 않은 JWT")
//    public void updateSurveyFail1() throws Exception {
//        // given
//        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
//        surveyRequestDto.setTitle("제목 수정");
//        when(surveyDocumentService.updateSurvey(any(HttpServletRequest.class), any(SurveyRequestDto.class), any(Long.class)))
//                .thenThrow(new InvalidUserException("올바르지 않은 유저입니다."));
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(put("/api/document/external/update/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(surveyRequestDto))
//                )
//                .andExpect(status().isUnauthorized())
//                .andDo(print())
//                .andReturn();
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "올바르지 않은 유저입니다.");
//    }
//
//    /**
//     * 설문 수정시
//     * 설문 수정을 요청한 유저의 JWT 토큰과
//     * 설문을 수정할 수 있는 권한의 JWT 토큰이 일치하지 않은 경우입니다.
//     *
//     * @throws Exception
//     */
//    @Test
//    @DisplayName("설문 수정 실패 - 권한 없는 유저의 요청")
//    public void updateSurveyFail2() throws Exception {
//        // given
//        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
//        surveyRequestDto.setTitle("제목 수정");
//        when(surveyDocumentService.updateSurvey(any(HttpServletRequest.class), any(SurveyRequestDto.class), any(Long.class)))
//                .thenThrow(new InvalidUserException("이 설문을 수정할 권한이 없습니다."));
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(put("/api/document/external/update/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(surveyRequestDto))
//                )
//                .andExpect(status().isUnauthorized())
//                .andDo(print())
//                .andReturn();
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "이 설문을 수정할 권한이 없습니다.");
//    }
//
//    /**
//     * 설문 수정시
//     * 설문 수정을 요청한 설문이 존재하지 않는 경우
//     *
//     * @throws Exception
//     */
//    @Test
//    @DisplayName("설문 수정 실패 - 수정할 설문이 존재하지 않는 경우")
//    public void updateSurveyFail3() throws Exception {
//        // given
//        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
//        surveyRequestDto.setTitle("제목 수정");
//        when(surveyDocumentService.updateSurvey(any(HttpServletRequest.class), any(SurveyRequestDto.class), any(Long.class)))
//                .thenThrow(new NotFoundException("설문"));
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(put("/api/document/external/update/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(surveyRequestDto))
//                )
//                .andExpect(status().isNotFound())
//                .andDo(print())
//                .andReturn();
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
//    }
//
//    @Test
//    @DisplayName("설문 삭제 성공")
//    public void deleteSurveySuccess() throws Exception {
//        // given
//        when(surveyDocumentService.deleteSurvey(any(HttpServletRequest.class), any(Long.class)))
//                .thenReturn(1L);
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/delete/1"))
//                .andExpect(status().isNoContent())
//                .andDo(print())
//                .andReturn();
//
//        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
//
//        // then
//        assertEquals(responseBody, "1");
//    }
//
//    /**
//     * 설문 삭제시
//     * 설문 삭제을 요청한 유저의 JWT 토큰이
//     * 올바르지 않은 JWT 토큰일 경우입니다.
//     *
//     * @throws Exception
//     */
//    @Test
//    @DisplayName("설문 삭제 실패 - 올바르지 않은 JWT")
//    public void deleteSurveyFail1() throws Exception {
//        // given
//        when(surveyDocumentService.deleteSurvey(any(HttpServletRequest.class), any(Long.class)))
//                .thenThrow(new InvalidUserException("올바르지 않은 유저입니다."));
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/delete/1"))
//                .andExpect(status().isUnauthorized())
//                .andDo(print())
//                .andReturn();
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "올바르지 않은 유저입니다.");
//    }
//
//    /**
//     * 설문 삭제시
//     * 설문 삭제을 요청한 유저의 JWT 토큰과
//     * 설문을 삭제할 수 있는 권한의 JWT 토큰이 일치하지 않은 경우입니다.
//     *
//     * @throws Exception
//     */
//    @Test
//    @DisplayName("설문 삭제 실패 - 권한 없는 유저의 요청")
//    public void deleteSurveyFail2() throws Exception {
//        // given
//        when(surveyDocumentService.deleteSurvey(any(HttpServletRequest.class), any(Long.class)))
//                .thenThrow(new InvalidUserException("이 설문을 삭제할 권한이 없습니다."));
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/delete/1"))
//                .andExpect(status().isUnauthorized())
//                .andDo(print())
//                .andReturn();
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "이 설문을 삭제할 권한이 없습니다.");
//    }
//
//    /**
//     * 설문 삭제시
//     * 설문 삭제을 요청한 설문이 존재하지 않는 경우
//     *
//     * @throws Exception
//     */
//    @Test
//    @DisplayName("설문 삭제 실패 - 삭제할 설문이 존재하지 않는 경우")
//    public void deleteSurveyFail3() throws Exception {
//        // given
//        when(surveyDocumentService.deleteSurvey(any(HttpServletRequest.class), any(Long.class)))
//                .thenThrow(new NotFoundException("설문"));
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/delete/1"))
//                .andExpect(status().isNotFound())
//                .andDo(print())
//                .andReturn();
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
//    }
//
//    @Test
//    @DisplayName("설문 기간 수정 성공")
//    public void managementDateSuccess() throws Exception {
//        // given
//        DateDto dateRequest = DateDto.builder()
//                .startDate(new Date())
//                .endDate(new Date())
//                .build();
//        when(surveyDocumentService.managementDate(any(Long.class), any(DateDto.class)))
//                .thenReturn(1L);
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/management/date/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dateRequest))
//                )
//                .andExpect(status().isNoContent())
//                .andDo(print())
//                .andReturn();
//
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "1");
//    }
//
//    /**
//     * 설문 기간 수정시
//     * 설문 기간 수정을 요청한 설문이 존재하지 않는 경우
//     *
//     * @throws Exception
//     */
//    @Test
//    @DisplayName("설문 기간 수정 실패 - 수정할 설문이 존재하지 않는 경우")
//    public void managementDateFail() throws Exception {
//        // given
//        DateDto dateRequest = DateDto.builder()
//                .startDate(new Date())
//                .endDate(new Date())
//                .build();
//        when(surveyDocumentService.managementDate(any(Long.class), any(DateDto.class)))
//                .thenThrow(new NotFoundException("설문"));
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/management/date/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dateRequest))
//                )
//                .andExpect(status().isNotFound())
//                .andDo(print())
//                .andReturn();
//
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
//    }
//
//    @Test
//    @DisplayName("설문 관리 조회 성공")
//    public void getManagementSuccess() throws Exception {
//        // given
//        ManagementResponseDto expectedResponse = ManagementResponseDto.builder()
//                .startDate(new Date())
//                .endDate(new Date())
//                .enable(true)
//                .build();
//        when(surveyDocumentService.managementSurvey(any(Long.class)))
//                .thenReturn(expectedResponse);
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(get("/api/document/external/management/1"))
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andReturn();
//
//        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
//        ManagementResponseDto actualResponse = objectMapper.readValue(responseBody, new TypeReference<>() {
//        });
//
//        // then
//        assertEquals(actualResponse.getEnable(), expectedResponse.getEnable());
//        assertEquals(actualResponse.getStartDate(), expectedResponse.getStartDate());
//        assertEquals(actualResponse.getEndDate(), expectedResponse.getEndDate());
//    }
//
//    /**
//     * 설문 관리 조회시
//     * 설문 관리 조회를 요청한 설문이 존재하지 않는 경우
//     *
//     * @throws Exception
//     */
//    @Test
//    @DisplayName("설문 관리 조회 실패 - 조회할 설문이 존재하지 않는 경우")
//    public void getManagementFail() throws Exception {
//        // given
//        when(surveyDocumentService.managementSurvey(any(Long.class)))
//                .thenThrow(new NotFoundException("설문"));
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(get("/api/document/external/management/1"))
//                .andExpect(status().isNotFound())
//                .andDo(print())
//                .andReturn();
//
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
//    }
//
//    @Test
//    @DisplayName("설문 활성화/비활성화 성공")
//    public void enableSurveySuccess() throws Exception {
//        // given
//        when(surveyDocumentService.managementEnable(any(), any()))
//                .thenReturn(true);
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/management/enable/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(true))
//                )
//                .andExpect(status().isNoContent())
//                .andDo(print())
//                .andReturn();
//
//        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
//
//        // then
//        assertEquals(responseBody, "true");
//    }
//
//    /**
//     * 설문 활성화/비활성화시
//     * 설문 활성화/비활성화를 요청한 설문이 존재하지 않는 경우
//     *
//     * @throws Exception
//     */
//    @Test
//    @DisplayName("설문 활성화/비활성화 실패 - 활성화/비활성화할 설문이 존재하지 않는 경우")
//    public void enableSurveyFail() throws Exception {
//        // given
//        when(surveyDocumentService.managementEnable(any(), any()))
//                .thenThrow(new NotFoundException("설문"));
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/management/enable/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(true))
//                )
//                .andExpect(status().isNotFound())
//                .andDo(print())
//                .andReturn();
//
//        // then
//        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
//    }
//
//
//    private PageRequestDto createPageRequestDto() {
//        return new PageRequestDto("grid", 0, "date", "ascending");
//    }
//
//    private Page<SurveyPageDto> createExpectedPage() {
//        List<SurveyPageDto> content = new ArrayList<>();
//
//        // 총 11개(1일부터 11일까지)의 설문 리스트 생성
//        for (int number = 1; number < 12; number++) {
//            SurveyPageDto surveyPageDto = new SurveyPageDto("test" + number,
//                    new Date());
//            content.add(surveyPageDto);
//        }
//
//        return new PageImpl<>(content, PageRequest.of(0, 10), content.size());
//    }

}
