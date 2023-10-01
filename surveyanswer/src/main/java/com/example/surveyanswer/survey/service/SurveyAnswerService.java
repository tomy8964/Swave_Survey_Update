package com.example.surveyanswer.survey.service;

import com.example.surveyanswer.survey.domain.QuestionAnswer;
import com.example.surveyanswer.survey.domain.SurveyAnswer;
import com.example.surveyanswer.survey.exception.InvalidReliabilityException;
import com.example.surveyanswer.survey.repository.questionAnswer.QuestionAnswerRepository;
import com.example.surveyanswer.survey.repository.surveyAnswer.SurveyAnswerRepository;
import com.example.surveyanswer.survey.request.ReliabilityChoice;
import com.example.surveyanswer.survey.request.ReliabilityQuestion;
import com.example.surveyanswer.survey.response.*;
import com.example.surveyanswer.survey.restAPI.service.RestAPIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyAnswerService {
    private static final int RELIABILITY_QUESTION_SIZE = 6;
    private static final List<ReliabilityQuestion> reliabilityQuestionList = initReliabilityQuestion();

    private final SurveyAnswerRepository surveyAnswerRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final RestAPIService restAPIService;

    @NotNull
    private static List<ReliabilityQuestion> initReliabilityQuestion() {
        List<ReliabilityQuestion> reliabilityQuestionList = new ArrayList<>();
        List<ReliabilityChoice> choiceList1 = new ArrayList<>();

        choiceList1.add(new ReliabilityChoice("매우 부정적이다"));
        choiceList1.add(new ReliabilityChoice("약간 부정적이다"));
        choiceList1.add(new ReliabilityChoice("어느 것도 아니다"));
        choiceList1.add(new ReliabilityChoice("약간 긍정적이다"));
        choiceList1.add(new ReliabilityChoice("매우 긍정적이다"));

        for (int i = 0; i < choiceList1.size(); i++) {
            ReliabilityQuestion reliabilityQuestion = new ReliabilityQuestion("이 문항에는 " + choiceList1.get(i).getChoiceName() + "를 선택해주세요.", 2, choiceList1, choiceList1.get(i).getChoiceName());
            reliabilityQuestionList.add(reliabilityQuestion);
        }

        List<ReliabilityChoice> choiceList2 = new ArrayList<>();

        choiceList2.add(new ReliabilityChoice("전혀 아니다"));
        choiceList2.add(new ReliabilityChoice("아니다"));
        choiceList2.add(new ReliabilityChoice("잘 모르겠다"));
        choiceList2.add(new ReliabilityChoice("어느 정도 아니다"));
        choiceList2.add(new ReliabilityChoice("매우 그렇다"));

        ReliabilityQuestion reliabilityQuestion = new ReliabilityQuestion("이 설문에 진심으로 참여하고 있나요?", 2, choiceList2, "매우 그렇다");
        reliabilityQuestionList.add(reliabilityQuestion);

        return reliabilityQuestionList;
    }

    private static ReliabilityQuestion getReliabilityQuestion() {

        int randomReliabilityQuestionNumber = new Random().nextInt(RELIABILITY_QUESTION_SIZE);

        return reliabilityQuestionList.get(randomReliabilityQuestionNumber);
    }

    private static QuestionResponseDto checkValidResponseByReliabilityTest(@NotNull SurveyResponseDto surveyResponse) {
        QuestionResponseDto reliabilityQuestion = null;
        for (QuestionResponseDto questionResponseDto : surveyResponse.getQuestionResponse()) {
            if (questionResponseDto.getAnswerId() == -1L) {
                for (ReliabilityQuestion question : reliabilityQuestionList) {
                    if (questionResponseDto.getTitle().equals(question.getTitle())) {
                        if (questionResponseDto.getAnswer().equals(question.getCorrectAnswer())) {
                            reliabilityQuestion = questionResponseDto;
                        } else {
                            throw new InvalidReliabilityException();
                        }
                    }
                }
            }
        }
        return reliabilityQuestion;
    }

    // 설문 응답 참여
    public SurveyDetailDto getParticipantSurvey(Long id) {
        if (restAPIService.getSurveyDetailDto(id).getReliability()) {
            return addReliabilityTest(restAPIService.getSurveyDetailDto(id));
        }
        return restAPIService.getSurveyDetailDto(id);
    }

    // 설문 응답 저장
    public void createSurveyAnswer(@NotNull SurveyResponseDto surveyResponse) {
        if (surveyResponse.getReliability()) {
            QuestionResponseDto reliabilityQuestion = checkValidResponseByReliabilityTest(surveyResponse);
            surveyResponse.getQuestionResponse().remove(reliabilityQuestion);
        }
        Long surveyDocumentId = surveyResponse.getId();

        SurveyAnswer surveyAnswer = SurveyAnswer.builder()
                .surveyDocumentId(surveyDocumentId)
                .title(surveyResponse.getTitle())
                .description(surveyResponse.getDescription())
                .type(surveyResponse.getType())
                .build();

        surveyAnswerRepository.save(surveyAnswer);

        for (QuestionResponseDto questionResponseDto : surveyResponse.getQuestionResponse()) {
            QuestionAnswer questionAnswer = QuestionAnswer.builder()
                    .surveyAnswer(surveyAnswer)
                    .title(questionResponseDto.getTitle())
                    .questionType(questionResponseDto.getType())
                    .checkAnswer(questionResponseDto.getAnswer())
                    .checkAnswerId(questionResponseDto.getAnswerId())
                    .surveyDocumentId(surveyDocumentId)
                    .build();
            questionAnswerRepository.save(questionAnswer);
            // if 찬부식 or 객관식
            // if 주관식 -> checkId에 주관식인 questionId가 들어감
            if (questionResponseDto.getType() != 0) {
                //check 한 answer 의 id 값으로 survey document 의 choice 를 찾아서 count ++
                if (questionAnswer.getCheckAnswerId() != null) {
                    restAPIService.giveChoiceIdToCount(questionAnswer.getCheckAnswerId());
                }
            }
        }
        surveyAnswerRepository.save(surveyAnswer);
        //count Answer
        restAPIService.giveDocumentIdtoCountResponse(surveyDocumentId);

        //REST API to survey analyze controller
        restAPIService.startAnalyze(surveyDocumentId);
    }

    public List<QuestionAnswer> getQuestionAnswers(Long questionDocumentId) {
        return questionAnswerRepository.findQuestionAnswersByCheckAnswerId(questionDocumentId);
    }

    public List<QuestionAnswerDto> getQuestionAnswerByCheckAnswerId(Long id) {
        List<QuestionAnswer> questionAnswers = questionAnswerRepository.findQuestionAnswersByCheckAnswerId(id);
        List<QuestionAnswerDto> questionAnswerDtoList = new ArrayList<>();
        for (QuestionAnswer questionAnswer : questionAnswers) {
            QuestionAnswerDto questionAnswerDto = new QuestionAnswerDto();
            questionAnswerDto.setCheckAnswer(questionAnswer.getCheckAnswer());
            questionAnswerDto.setQuestionType(questionAnswer.getQuestionType());
            questionAnswerDto.setId(questionAnswer.getId());
            questionAnswerDtoList.add(questionAnswerDto);
        }
        return questionAnswerDtoList;
    }

    public List<SurveyAnswer> getSurveyAnswersBySurveyDocumentId(Long id) {
        return surveyAnswerRepository.findSurveyAnswersBySurveyDocumentId(id);
    }

    // 진정성 검사 추가 검증
    private SurveyDetailDto addReliabilityTest(SurveyDetailDto surveyDetailDto) {

        QuestionDetailDto reliabilityQuestionDto;
        ReliabilityQuestion reliabilityQuestion;

        reliabilityQuestion = getReliabilityQuestion();
        reliabilityQuestionDto = reliabilityQuestion.toQuestionDetailDto();

        surveyDetailDto.getQuestionList().add(new Random().nextInt(surveyDetailDto.getQuestionList().size()), reliabilityQuestionDto);

        return surveyDetailDto;
    }
}