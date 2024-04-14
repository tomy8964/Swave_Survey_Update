package com.example.surveydocument;

import com.example.surveydocument.survey.domain.PageResponseDto;
import com.example.surveydocument.survey.domain.SurveyDocument;
import com.example.surveydocument.survey.repository.surveyDocument.SurveyDocumentRepository;
import com.example.surveydocument.survey.request.DateDto;
import com.example.surveydocument.survey.request.PageRequestDto;
import com.example.surveydocument.survey.request.SurveyRequestDto;
import com.example.surveydocument.survey.response.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.surveydocument.survey.controller.SurveyDocumentExternalControllerTest.createSurveyRequestDto;
import static com.example.surveydocument.survey.service.SurveyDocumentServiceTest.createExpectedPage;
import static com.example.surveydocument.survey.service.SurveyDocumentServiceTest.createPageRequestDto;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class SurveyDocumentIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    MockWebServer mockBackEnd;
    @Autowired
    SurveyDocumentRepository surveyDocumentRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private ObjectMapper mapper = new ObjectMapper();

    @AfterEach
    public void cleanCache() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    @Transactional
    @DisplayName("설문 2개 생성 성공")
    public void createSurveySuccess() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        mockBackEnd.enqueue(createMockResponse(userId));

        // when
        mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();
        mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();

        // then
        List<SurveyDocument> all = surveyDocumentRepository.findAll();
        assertEquals(all.size(), 2);
    }

    /**
     * 이 테스트는 설문 생성을 실패하는 경우를 검증합니다.
     * 설문 생성시 설문 생성을 요청한 유저의 JWT 토큰이
     * 올바르지 않거나 권한이 없는 경우입니다.
     * 이 경우 Service에서 InvalidUserException을 발생시킵니다.
     *
     * @throws Exception JSON Parsing 예외를 던집니다.
     * @RestControllerAdvice 에서 이를 감지해 에러를 핸들링합니다.
     */
    @Test
    @Transactional
    @DisplayName("설문 생성 실패 - 올바르지 않은 JWT 토큰")
    public void createSurveyFail() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(null));

        // when
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(createResult.getResponse().getContentAsString(), "유저 정보가 올바르지 않습니다.");
    }

    @Test
    @Transactional
    @DisplayName("설문 목록 조회 성공")
    public void readListSuccess() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        for (int i = 0; i < 10; i++) {
            mockBackEnd.enqueue(createMockResponse(userId));
            mockMvc.perform(post("/api/document/external/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(surveyRequestDto))
                    )
                    .andExpect(status().isCreated());
        }

        PageRequestDto pageRequestDto = createPageRequestDto();
        Page<SurveyPageDto> expectedPage = createExpectedPage();

        // when
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult mvcResult = mockMvc.perform(post("/api/document/external/survey-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(pageRequestDto))
                )
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        PageResponseDto<SurveyPageDto> actualPage = mapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(actualPage.getContent().size(), expectedPage.getContent().size());
        List<SurveyDocument> all = surveyDocumentRepository.findAll();
        assertEquals(all.size(), 10);
    }

    /**
     * 설문 목록 조회시
     * 설문 목록 조회를 요청한 유저의 JWT 토큰이
     * 권한이 없는 경우 혹은 올바르지 않은 JWT 토큰일 경우입니다.
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("설문 목록 조회 실패 - 올바르지 않은 JWT 토큰")
    public void readListFail() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(userId));
        mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated());

        PageRequestDto pageRequestDto = createPageRequestDto();

        // when
        mockBackEnd.enqueue(createMockResponse(null));
        MvcResult mvcResult = mockMvc.perform(post("/api/document/external/survey-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(pageRequestDto))
                )
                .andExpect(status().isUnauthorized())
                .andReturn();

        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "유저 정보가 올바르지 않습니다.");
    }

    @Test
    @Transactional
    @DisplayName("설문 조회 성공")
    public void readSurveySuccess() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        List<QuestionAnswerDto> list = new ArrayList<>();
        list.add(QuestionAnswerDto.builder()
                .questionType(0)
                .checkAnswer("주관식 답변입니다.")
                .build());
        // when
        mockBackEnd.enqueue(createMockResponse(list));
        MvcResult mvcResult = mockMvc.perform(get("/api/document/external/survey-list/" + createdSurveyID))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        SurveyDetailDto actualResponse = mapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(actualResponse.getId().toString(), createdSurveyID);
    }

    /**
     * 설문 조회시
     * 설문 조회를 요청한 설문이 존재하지 않는 경우
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("설문 조회 실패 - 조회할 설문이 존재하지 않는 경우")
    public void readSurveyFail1() throws Exception {
        // given

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/external/survey-list/1"))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
    }

    @Test
    @Transactional
    @DisplayName("설문 수정 성공")
    public void updateSurveySuccess() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        surveyRequestDto.setTitle("제목 수정");
        mockBackEnd.enqueue(createMockResponse(userId));

        // when
        MvcResult mvcResult = mockMvc.perform(put("/api/document/external/update/" + createdSurveyID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        SurveyDocument surveyDocument = surveyDocumentRepository.findSurveyById(Long.valueOf(responseBody)).get();
        // then
        assertEquals(surveyDocument.getTitle(), "제목 수정");
    }

    @Test
    @Transactional
    @DisplayName("설문 수정 실패 - 존재 하지 않는 설문")
    public void updateSurveyFail1() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated());
        surveyRequestDto.setTitle("제목 수정");

        // when
        MvcResult mvcResult = mockMvc.perform(put("/api/document/external/update/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
    }

    /**
     * 설문 수정시
     * 설문 수정을 요청한 유저의 JWT 토큰과
     * 설문을 수정할 수 있는 권한의 JWT 토큰이 일치하지 않은 경우입니다.
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("설문 수정 실패 - 권한 없는 유저의 요청")
    public void updateSurveyFail2() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        surveyRequestDto.setTitle("제목 수정");
        mockBackEnd.enqueue(createMockResponse(2L));

        // when
        MvcResult mvcResult = mockMvc.perform(put("/api/document/external/update/" + createdSurveyID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "이 설문을 수정할 권한이 없습니다.");
    }

    @Test
    @Transactional
    @DisplayName("설문 수정 실패 - 올바르지 않은 JWT 토큰")
    public void updateSurveyFail3() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        surveyRequestDto.setTitle("제목 수정");
        mockBackEnd.enqueue(createMockResponse(null));

        // when
        MvcResult mvcResult = mockMvc.perform(put("/api/document/external/update/" + createdSurveyID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "유저 정보가 올바르지 않습니다.");
    }

    @Test
    @Transactional
    @DisplayName("설문 삭제 성공")
    public void deleteSurveySuccess() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        mockBackEnd.enqueue(createMockResponse(userId));

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/delete/" + createdSurveyID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        // then
        assertEquals(responseBody, createdSurveyID);
    }

    @Test
    @Transactional
    @DisplayName("설문 삭제 실패 - 존재 하지 않는 설문")
    public void deleteSurveyFail1() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated());

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/delete/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
    }

    /**
     * 설문 수정시
     * 설문 수정을 요청한 유저의 JWT 토큰과
     * 설문을 수정할 수 있는 권한의 JWT 토큰이 일치하지 않은 경우입니다.
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("설문 삭제 실패 - 권한 없는 유저의 요청")
    public void deleteSurveyFail2() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        mockBackEnd.enqueue(createMockResponse(2L));

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/delete/" + createdSurveyID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "이 설문을 삭제할 권한이 없습니다.");
    }

    @Test
    @Transactional
    @DisplayName("설문 삭제 실패 - 올바르지 않은 JWT 토큰")
    public void deleteSurveyFail3() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        mockBackEnd.enqueue(createMockResponse(null));

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/delete/" + createdSurveyID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "유저 정보가 올바르지 않습니다.");
    }

    @Test
    @Transactional
    @DisplayName("설문 기간 수정 성공")
    public void managementDateSuccess() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        DateDto dateDto = DateDto.builder()
                .startDate(new Date())
                .endDate(new Date())
                .build();

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/management/date/" + createdSurveyID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dateDto))
                )
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        // then
        assertEquals(responseBody, createdSurveyID);
    }

    /**
     * 설문 기간 수정시
     * 설문 기간 수정을 요청한 설문이 존재하지 않는 경우
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("설문 기간 수정 실패 - 수정할 설문이 존재하지 않는 경우")
    public void managementDateFail() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated());
        DateDto dateDto = DateDto.builder()
                .startDate(new Date())
                .endDate(new Date())
                .build();

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/management/date/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dateDto))
                )
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
    }

    @Test
    @Transactional
    @DisplayName("설문 관리 조회 성공")
    public void getManagementSuccess() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/external/management/" + createdSurveyID))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ManagementResponseDto actualResponse = mapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(surveyRequestDto.getDate().getEnable(), actualResponse.getEnable());
        assertEquals(surveyRequestDto.getDate().getStartDate(), actualResponse.getStartDate());
        assertEquals(surveyRequestDto.getDate().getEndDate(), actualResponse.getEndDate());
    }

    /**
     * 설문 관리 조회시
     * 설문 관리 조회를 요청한 설문이 존재하지 않는 경우
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("설문 관리 조회 실패 - 조회할 설문이 존재하지 않는 경우")
    public void getManagementFail() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated());

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/external/management/300"))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();

        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
    }

    @Test
    @Transactional
    @DisplayName("설문 활성화/비활성화 성공")
    public void enableSurveySuccess() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/management/enable/" + createdSurveyID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(true))
                )
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // then
        assertEquals(responseBody, "true");
    }

    /**
     * 설문 활성화/비활성화시
     * 설문 활성화/비활성화를 요청한 설문이 존재하지 않는 경우
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("설문 활성화/비활성화 실패 - 활성화/비활성화할 설문이 존재하지 않는 경우")
    public void enableSurvey2Fail() throws Exception {
        // given
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        Long userId = 1L;
        mockBackEnd.enqueue(createMockResponse(userId));
        mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated());

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/document/external/management/enable/300")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(true))
                )
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();

        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
    }

    @Test
    @Transactional
    @DisplayName("응답 서비스에서 설문 조회 성공")
    public void readSurvey1Success() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        List<QuestionAnswerDto> list = new ArrayList<>();
        list.add(QuestionAnswerDto.builder()
                .questionType(0)
                .checkAnswer("주관식 답변입니다.")
                .build());
        // when
        mockBackEnd.enqueue(createMockResponse(list));
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getSurveyDocumentToAnswer/" + createdSurveyID))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        SurveyDetailDto actualResponse = mapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(actualResponse.getId().toString(), createdSurveyID);
    }

    /**
     * 설문 조회시
     * 설문 조회를 요청한 설문이 존재하지 않는 경우
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("응답 서비스에서 설문 조회 실패 - 조회할 설문이 존재하지 않는 경우")
    public void readSurvey1Fail1() throws Exception {
        // given

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getSurveyDocumentToAnswer/1"))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
    }

    @Test
    @Transactional
    @DisplayName("분석 서비스에서 설문 조회 성공")
    public void readSurvey2Success() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        List<QuestionAnswerDto> list = new ArrayList<>();
        list.add(QuestionAnswerDto.builder()
                .questionType(0)
                .checkAnswer("주관식 답변입니다.")
                .build());
        // when
        mockBackEnd.enqueue(createMockResponse(list));
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getSurveyDocumentToAnalyze/" + createdSurveyID))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        SurveyDetailDto actualResponse = mapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(actualResponse.getId().toString(), createdSurveyID);
    }

    /**
     * 설문 조회시
     * 설문 조회를 요청한 설문이 존재하지 않는 경우
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("분석 서비스에서 설문 조회 실패 - 조회할 설문이 존재하지 않는 경우")
    public void readSurvey2Fail1() throws Exception {
        // given

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getSurveyDocumentToAnalyze/1"))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 설문입니다.");
    }

    @Test
    @Transactional
    @DisplayName("선택지 응답자 수 증가 성공")
    void countChoiceSuccess() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        Long choiceId = surveyDocumentRepository.findById(Long.valueOf(createdSurveyID)).get().getQuestionDocumentList().get(0).getChoiceList().get(0).getId();

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/document/internal/count/" + choiceId)
                )
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // then
        assertEquals(choiceId.toString(), responseBody);
    }

    @Test
    @Transactional
    @DisplayName("선택지 응답자 수 증가 실패 - 존재하지 않는 선택지")
    void countChoiceFail() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/document/internal/count/300")
                )
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // then
        assertEquals(responseBody, "존재하지 않는 선택지입니다.");
    }

    @Test
    @Transactional
    @DisplayName("설문 응답자 수 증가 성공")
    void countAnswerSuccess() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        Long surveyId = surveyDocumentRepository.findById(Long.valueOf(createdSurveyID)).get().getId();
        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/document/internal/countAnswer/" + surveyId)
                )
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // then
        assertEquals(surveyId.toString(), responseBody);
    }

    @Test
    @Transactional
    @DisplayName("설문 응답자 수 증가 실패 - 존재하지 않는 설문")
    void countAnswerFail() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(userId));
        mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated());

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/document/internal/countAnswer/300")
                )
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // then
        assertEquals(responseBody, "존재하지 않는 설문입니다.");
    }

    @Test
    @Transactional
    @DisplayName("선택지 조회 성공")
    public void getChoiceSuccess() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        Long choiceID = surveyDocumentRepository.findSurveyById(Long.valueOf(createdSurveyID)).get().getQuestionDocumentList().get(0).getChoiceList().get(0).getId();


        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getChoice/" + choiceID))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ChoiceDetailDto actualResponse = mapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(actualResponse.getId(), choiceID);
    }

    /**
     * 선택지 조회시
     * 조회를 요청한 선택지가 존재하지 않는 경우
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("선택지 조회 실패 - 조회할 선택지가 존재하지 않는 경우")
    public void getChoiceFail() throws Exception {
        // given

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getChoice/300"))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 선택지입니다.");
    }

    @Test
    @Transactional
    @DisplayName("문항 조회 성공")
    public void getQuestionSuccess() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        Long questionId = surveyDocumentRepository.findSurveyById(Long.valueOf(createdSurveyID)).get().getQuestionDocumentList().get(0).getId();

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getQuestion/" + questionId))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        QuestionDetailDto actualResponse = mapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(actualResponse.getId(), questionId);
    }

    /**
     * 문항 조회시
     * 조회를 요청한 문항이 존재하지 않는 경우
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("문항 조회 실패 - 조회할 문항이 존재하지 않는 경우")
    public void getQuestionFail() throws Exception {
        // given

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getQuestion/300"))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 문항입니다.");
    }

    @Test
    @Transactional
    @DisplayName("선택지로 문항 조회 성공")
    public void getQuestionByChoiceIdSuccess() throws Exception {
        // given
        Long userId = 1L;
        SurveyRequestDto surveyRequestDto = createSurveyRequestDto();
        mockBackEnd.enqueue(createMockResponse(userId));
        MvcResult createResult = mockMvc.perform(post("/api/document/external/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(surveyRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String createdSurveyID = createResult.getResponse().getContentAsString();
        Long choiceId = surveyDocumentRepository.findSurveyById(Long.valueOf(createdSurveyID)).get().getQuestionDocumentList().get(0).getChoiceList().get(0).getId();

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getQuestionByChoiceId/" + choiceId))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        QuestionDetailDto actualResponse = mapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(actualResponse.getId(), choiceId);
    }

    /**
     * 문항 조회시
     * 선택지로 조회를 요청한 문항이 존재하지 않는 경우
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @DisplayName("선택지로 문항 조회 실패 - 조회할 문항이 존재하지 않는 경우")
    public void getQuestionByChoiceIdFail() throws Exception {
        // given

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getQuestionByChoiceId/300"))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();
        // then
        assertEquals(mvcResult.getResponse().getContentAsString(), "존재하지 않는 문항입니다.");
    }

    private MockResponse createMockResponse(Object body) throws JsonProcessingException {
        return new MockResponse()
                .setBody(mapper.writeValueAsString(body))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

}
