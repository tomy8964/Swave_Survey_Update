package com.example.surveydocument.survey.controller;

import com.example.surveydocument.survey.response.ChoiceDetailDto;
import com.example.surveydocument.survey.response.QuestionDetailDto;
import com.example.surveydocument.survey.response.SurveyDetailDto;
import com.example.surveydocument.survey.response.SurveyDetailDto2;
import com.example.surveydocument.survey.service.SurveyDocumentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.example.surveydocument.survey.controller.SurveyDocumentExternalControllerTest.createSurveyDetailDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SurveyDocumentInternalController.class)
public class SurveyDocumentInternalControllerTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SurveyDocumentService surveyDocumentService;

    public static SurveyDetailDto2 createSurveyDetailDto2() {

        List<ChoiceDetailDto> choiceDetailDtoList = new ArrayList<>();
        ChoiceDetailDto choiceRequest1 = ChoiceDetailDto.builder()
                .id(1L)
                .title("객관식 1번")
                .count(0)
                .build();
        choiceDetailDtoList.add(choiceRequest1);
        ChoiceDetailDto choiceRequest2 = ChoiceDetailDto.builder()
                .id(2L)
                .title("객관식 2번")
                .count(0)
                .build();
        choiceDetailDtoList.add(choiceRequest2);

        List<QuestionDetailDto> questionDetailDtoList = new ArrayList<>();
        QuestionDetailDto questionDetail1 = QuestionDetailDto.builder()
                .id(1L)
                .title("객관식 설문 문항")
                .questionType(1)
                .choiceList(choiceDetailDtoList)
                .build();

        QuestionDetailDto questionDetail2 = QuestionDetailDto.builder()
                .id(2L)
                .title("주관식 설문 문항")
                .choiceList(null)
                .questionType(0)
                .build();
        questionDetailDtoList.add(questionDetail1);
        questionDetailDtoList.add(questionDetail2);

        return SurveyDetailDto2.builder()
                .id(1L)
                .title("설문 제목")
                .description("설문 내용")
                .countAnswer(0)
                .questionList(questionDetailDtoList)
                .build();
    }

    @Test
    @DisplayName("설문 응답 서비스로 응답 성공")
    void responseToAnswerSuccess() throws Exception {
        // given
        SurveyDetailDto expectedResponse = createSurveyDetailDto();
        when(surveyDocumentService.readSurveyDetail(any()))
                .thenReturn(expectedResponse);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getSurveyDocumentToAnswer/1")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        verify(surveyDocumentService).readSurveyDetail(1L);

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        SurveyDetailDto actualResponse = objectMapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(actualResponse.getId(), expectedResponse.getId());
        assertEquals(actualResponse.getTitle(), expectedResponse.getTitle());
        assertEquals(actualResponse.getDescription(), expectedResponse.getDescription());
        assertEquals(actualResponse.getCountAnswer(), expectedResponse.getCountAnswer());
        assertEquals(actualResponse.getReliability(), expectedResponse.getReliability());

        assertEquals(actualResponse.getQuestionList().size(), expectedResponse.getQuestionList().size());
    }

    @Test
    @DisplayName("설문 분석 서비스로 응답 성공")
    void responseToAnalyzeSuccess() throws Exception {
        // given
        SurveyDetailDto2 expectedResponse = createSurveyDetailDto2();
        when(surveyDocumentService.readSurveyDetail2(any()))
                .thenReturn(expectedResponse);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getSurveyDocumentToAnalyze/1")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        verify(surveyDocumentService).readSurveyDetail2(1L);

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        SurveyDetailDto actualResponse = objectMapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(actualResponse.getId(), expectedResponse.getId());
        assertEquals(actualResponse.getTitle(), expectedResponse.getTitle());
        assertEquals(actualResponse.getDescription(), expectedResponse.getDescription());
        assertEquals(actualResponse.getCountAnswer(), expectedResponse.getCountAnswer());

        assertEquals(actualResponse.getQuestionList().size(), expectedResponse.getQuestionList().size());
    }

    @Test
    @DisplayName("선택지 응답자 수 증가 테스트")
    void countChoice() throws Exception {
        // given
        Long choiceId = 1L;
        when(surveyDocumentService.countChoice(any())).thenReturn(choiceId);

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/document/internal/count/1")
                )
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn();

        verify(surveyDocumentService).countChoice(choiceId);

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // then
        assertEquals(choiceId.toString(), responseBody);
    }

    @Test
    @DisplayName("설문 응답자 수 증가 테스트")
    void countAnswer() throws Exception {
        // given
        Long surveyDocumentId = 1L;
        when(surveyDocumentService.countSurveyDocument(any())).thenReturn(surveyDocumentId);

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/document/internal/countAnswer/1")
                )
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn();

        verify(surveyDocumentService).countSurveyDocument(surveyDocumentId);

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        // then
        assertEquals(surveyDocumentId.toString(), responseBody);
    }

    @Test
    @DisplayName("선택지 조회 테스트")
    void getChoice() throws Exception {
        // given
        ChoiceDetailDto choiceDetailDto = ChoiceDetailDto.builder()
                .id(1L)
                .title("객관식 1번")
                .count(0)
                .build();
        when(surveyDocumentService.getChoice(any())).thenReturn(choiceDetailDto);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getChoice/1")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        verify(surveyDocumentService).getChoice(choiceDetailDto.getId());

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ChoiceDetailDto actualResponse = objectMapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(choiceDetailDto.getId(), actualResponse.getId());
        assertEquals(choiceDetailDto.getTitle(), actualResponse.getTitle());
        assertEquals(choiceDetailDto.getCount(), actualResponse.getCount());
    }

    @Test
    @DisplayName("문항 조회 테스트")
    void getQuestion() throws Exception {
        // given
        QuestionDetailDto questionDetailDto = QuestionDetailDto.builder()
                .id(1L)
                .title("객관식 문항입니다.")
                .questionType(0)
                .build();
        when(surveyDocumentService.getQuestion(any())).thenReturn(questionDetailDto);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getQuestion/1")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        verify(surveyDocumentService).getQuestion(questionDetailDto.getId());

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        QuestionDetailDto actualResponse = objectMapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(questionDetailDto.getId(), actualResponse.getId());
        assertEquals(questionDetailDto.getTitle(), actualResponse.getTitle());
        assertEquals(questionDetailDto.getQuestionType(), actualResponse.getQuestionType());
    }

    @Test
    @DisplayName("선택지로 문항 조회 테스트")
    void getQuestionByChoiceId() throws Exception {
        // given
        Long choiceId = 1L;
        QuestionDetailDto questionDetailDto = QuestionDetailDto.builder()
                .id(1L)
                .title("객관식 문항입니다.")
                .questionType(0)
                .build();
        when(surveyDocumentService.getQuestionByChoiceId(any())).thenReturn(questionDetailDto);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/document/internal/getQuestionByChoiceId/1")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        verify(surveyDocumentService).getQuestionByChoiceId(choiceId);

        String responseBody = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        QuestionDetailDto actualResponse = objectMapper.readValue(responseBody, new TypeReference<>() {
        });

        // then
        assertEquals(questionDetailDto.getId(), actualResponse.getId());
        assertEquals(questionDetailDto.getTitle(), actualResponse.getTitle());
        assertEquals(questionDetailDto.getQuestionType(), actualResponse.getQuestionType());
    }
}