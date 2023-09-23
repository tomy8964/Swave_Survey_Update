package com.example.surveyanswer.survey.service;

import com.example.surveyanswer.survey.domain.*;
import com.example.surveyanswer.survey.exception.InvalidTokenException;
import com.example.surveyanswer.survey.repository.questionAnswer.QuestionAnswerRepository;
import com.example.surveyanswer.survey.repository.surveyAnswer.SurveyAnswerRepository;
import com.example.surveyanswer.survey.request.ReliabilityChoice;
import com.example.surveyanswer.survey.request.ReliabilityQuestion;
import com.example.surveyanswer.survey.request.ReliabilityQuestionRequest;
import com.example.surveyanswer.survey.response.*;
import com.example.surveyanswer.survey.restAPI.service.RestAPIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyAnswerService {
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final RestAPIService restAPIService;
    private List<ReliabilityQuestion> questions;
    private int reliabilityquestionNumber;

    Random random = new Random();

    // 설문 응답 참여
    public SurveyDetailDto getParticipantSurvey(Long id){
        return getSurveyDetailDto(id);
    }

    public ReliabilityQuestion reliabilityQuestion() throws JsonProcessingException {
        String jsonString = "{\"questionRequest\":[" +
                "{\"title\":\"이 문항에는 어느것도 아니다를 선택해주세요.\",\"type\":2,\"correct_answer\":\"어느것도 아니다.\",\"choiceList\":[{\"id\":1,\"choiceName\":\"매우 부정적이다.\"},{\"id\":2,\"choiceName\":\"약간 부정적이다.\"},{\"id\":3,\"choiceName\":\"어느것도 아니다.\"},{\"id\":4,\"choiceName\":\"약간 긍정적이다..\"},{\"id\":5,\"choiceName\":\"매우 긍정적이다.\"}]}," +
                "{\"title\":\"이 문항에는 매우 부정적이다를 선택해주세요.\",\"correct_answer\":\"매우 부정적이다.\",\"type\":2,\"choiceList\":[{\"id\":1,\"choiceName\":\"매우 부정적이다.\"},{\"id\":2,\"choiceName\":\"약간 부정적이다.\"},{\"id\":3,\"choiceName\":\"어느것도 아니다.\"},{\"id\":4,\"choiceName\":\"약간 긍정적이다..\"},{\"id\":5,\"choiceName\":\"매우 긍정적이다.\"}]}," +
                "{\"title\":\"이 문항에는 약간 부정적이다를 선택해주세요.\",\"correct_answer\":\"약간 부정적이다.\",\"type\":2,\"choiceList\":[{\"id\":1,\"choiceName\":\"매우 부정적이다.\"},{\"id\":2,\"choiceName\":\"약간 부정적이다.\"},{\"id\":3,\"choiceName\":\"어느것도 아니다.\"},{\"id\":4,\"choiceName\":\"약간 긍정적이다..\"},{\"id\":5,\"choiceName\":\"매우 긍정적이다.\"}]}," +
                "{\"title\":\"이 문항에는 약간 긍정적이다를 선택해주세요.\",\"correct_answer\":\"약간 긍정적이다.\",\"type\":2,\"choiceList\":[{\"id\":1,\"choiceName\":\"매우 부정적이다.\"},{\"id\":2,\"choiceName\":\"약간 부정적이다.\"},{\"id\":3,\"choiceName\":\"어느것도 아니다.\"},{\"id\":4,\"choiceName\":\"약간 긍정적이다..\"},{\"id\":5,\"choiceName\":\"매우 긍정적이다.\"}]}," +
                "{\"title\":\"이 문항에는 매우 긍정적이다를 선택해주세요.\",\"correct_answer\":\"매우 긍정적이다.\",\"type\":2,\"choiceList\":[{\"id\":1,\"choiceName\":\"매우 부정적이다.\"},{\"id\":2,\"choiceName\":\"약간 부정적이다.\"},{\"id\":3,\"choiceName\":\"어느것도 아니다.\"},{\"id\":4,\"choiceName\":\"약간 긍정적이다..\"},{\"id\":5,\"choiceName\":\"매우 긍정적이다.\"}]}," +
                "{\"title\":\"이 질문에 대한 답변을 생각해보지 않고 무작위로 선택했습니다.\",\"correct_answer\":\"그렇다.\",\"type\":2,\"choiceList\":[{\"id\":1,\"choiceName\":\"그렇다.\"},{\"id\":2,\"choiceName\":\"그렇지 않다.\"},{\"id\":3,\"choiceName\":\"잘 모르겠다.\"},{\"id\":4,\"choiceName\":\"그렇다고 말할 수 있다.\"}]}," +
                "{\"title\":\"이 설문에 진심으로 참여하고 있나요?\",\"correct_answer\":\"매우 그렇다.\",\"type\":2,\"choiceList\":[{\"id\":1,\"choiceName\":\"전혀 아니다.\"},{\"id\":2,\"choiceName\":\"아니다.\"},{\"id\":3,\"choiceName\":\"잘 모르겠다.\"},{\"id\":4,\"choiceName\":\"어느 정도 아니다.\"},{\"id\":5,\"choiceName\":\"매우 그렇다.\"}]}," +
                "{\"title\":\"메뚜기의 종류를 3000개이상 알고 있다\",\"correct_answer\":\"아니다.\",\"type\":2,\"choiceList\":[{\"id\":1,\"choiceName\":\"아니다.\"},{\"id\":2,\"choiceName\":\"그렇다.\"}]}," +
                "{\"title\":\"설문조사의 목적과 내용을 이해하고 진정성을 유지하며 응답하고 있습니까?\",\"correct_answer\":\"매우 그렇다.\",\"type\":2,\"choiceList\":[{\"id\":1,\"choiceName\":\"전혀 아니다.\"},{\"id\":2,\"choiceName\":\"아니다.\"},{\"id\":3,\"choiceName\":\"잘 모르겠다.\"},{\"id\":4,\"choiceName\":\"어느 정도 아니다.\"},{\"id\":5,\"choiceName\":\"매우 그렇다.\"}]}," +
                "{\"title\":\"이 설문조사에 참여하는 데 얼마나 진지하게 접근하고 있나요?\",\"correct_answer\":\"매우 진지하게 접근하고 있다.\",\"type\":2,\"choiceList\":[{\"id\":1,\"choiceName\":\"매우 진지하게 접근하고 있다.\"},{\"id\":2,\"choiceName\":\"부주의하게 접근하고 있다.\"},{\"id\":3,\"choiceName\":\"아주 부주의하게 접근하고 있다..\"}]}" +
                "]}";

        ObjectMapper objectMapper = new ObjectMapper();
        ReliabilityQuestionRequest questionRequest = objectMapper.readValue(jsonString, ReliabilityQuestionRequest.class);

        reliabilityquestionNumber=random.nextInt(10);

        // Access the converted Java object
        questions = questionRequest.getQuestionRequest();
        ReliabilityQuestion question1=questions.get(reliabilityquestionNumber);
        List<ReliabilityChoice> Rchoices = question1.getChoiceList();
        return question1;

//        for (ReliabilityQuestion question : questions) {
//            System.out.println("Title: " + question.getTitle());
//            System.out.println("Type: " + question.getType());
//            System.out.println("Correct Answer: " + question.getCorrect_answer());
//            List<ReliabilityChoice> Rchoices = question.getChoiceList();
//            for (ReliabilityChoice choice : Rchoices) {
//                System.out.println("Choice ID: " + choice.getId());
//                System.out.println("Choice Name: " + choice.getChoiceName());
//            }
//        }
    }
    // 설문 응답 저장
    public void createSurveyAnswer(SurveyResponseDto surveyResponse){
        if(surveyResponse.getReliability()) {
            for (QuestionResponseDto questionResponseDto : surveyResponse.getQuestionResponse()) {
                for (ReliabilityQuestion question : questions) {
                    if (questionResponseDto.getTitle().equals(question.getTitle())) {
                        System.out.println(question.getCorrect_answer());
                        System.out.println(question.getTitle());
                        if (questionResponseDto.getAnswer().equals(question.getCorrect_answer())) {
                            System.out.println(question.getCorrect_answer());
                            surveyResponse.getQuestionResponse().remove(questionResponseDto);
                            break;
                        } else {
                            return;
                        }
                    }
                    else{
                        return;
                    }
                }
            }
        }
        Long surveyDocumentId = surveyResponse.getId();
        // SurveyDocumentId를 통해 어떤 설문인지 가져옴
//        SurveyDocument surveyDocument = surveyDocumentRepository.findById(surveyDocumentId).get();

        // Survey Response 를 Survey Answer 에 저장하기
        SurveyAnswer surveyAnswer = SurveyAnswer.builder()
                .surveyDocumentId(surveyDocumentId)
                .title(surveyResponse.getTitle())
                .description(surveyResponse.getDescription())
                .type(surveyResponse.getType())
                .questionAnswerList(new ArrayList<>())
                .build();
        surveyAnswerRepository.save(surveyAnswer);

        // Survey Response 를 Question Answer 에 저장하기
        for (QuestionResponseDto questionResponseDto : surveyResponse.getQuestionResponse()) {
            // Question Answer 에 저장
            QuestionAnswer questionAnswer = QuestionAnswer.builder()
                    .surveyAnswerId(surveyAnswer)
                    .title(questionResponseDto.getTitle())
                    .questionType(questionResponseDto.getType())
                    .checkAnswer(questionResponseDto.getAnswer())
                    .checkAnswerId(questionResponseDto.getAnswerId())
                    .surveyDocumentId(surveyDocumentId)
                    .build();
            questionAnswerRepository.save(questionAnswer);
            // if 찬부식 or 객관식
            // if 주관식 -> checkId에 주관식인 questionId가 들어감
            if(questionResponseDto.getType()!=0){
                //check 한 answer 의 id 값으로 survey document 의 choice 를 찾아서 count ++
                if (questionAnswer.getCheckAnswerId() != null) {
                    restAPIService.giveChoiceIdToCount(questionAnswer.getCheckAnswerId());
                }
            }
            surveyAnswer.setQuestion(questionAnswer);
        }
        surveyAnswerRepository.flush();

        //count Answer
        restAPIService.giveDocumentIdtoCountAnswer(surveyDocumentId);
        // 저장된 설문 응답을 Survey 에 연결 및 저장
//        surveyDocument.setAnswer(surveyAnswer);
//        surveyDocumentRepository.flush();

        //REST API to survey analyze controller
        //todo:응답 할때 다시 수정
        restAPIService.restAPItoAnalyzeController(surveyDocumentId);
    }

    // todo : 분석 응답 리스트 불러오기
    public List<SurveyAnswer> readSurveyAnswerList(HttpServletRequest request, Long surveyId) throws InvalidTokenException {
        //Survey_Id를 가져와서 그 Survey 의 AnswerList 를 가져와야 함
        List<SurveyAnswer> surveyAnswerList = surveyAnswerRepository.findSurveyAnswersBySurveyDocumentId(surveyId);

        checkInvalidToken(request);

        return surveyAnswerList;
    }

    public List<QuestionAnswer> getQuestionAnswers(Long questionDocumentId){
        //Survey_Id를 가져와서 그 Survey 의 AnswerList 를 가져와야 함
        List<QuestionAnswer> questionAnswerList = questionAnswerRepository.findQuestionAnswersByCheckAnswerId(questionDocumentId);


        return questionAnswerList;
    }

    // 회원 유효성 검사, token 존재하지 않으면 예외처리
    private static void checkInvalidToken(HttpServletRequest request) throws InvalidTokenException {
        if(request.getHeader("Authorization") == null) {
            log.info("error");
            throw new InvalidTokenException();
        }
        log.info("토큰 체크 완료");
    }

    // SurveyDocument Response 보낼 SurveyDetailDto로 변환하는 메서드
    private SurveyDetailDto getSurveyDetailDto(Long surveyDocumentId) {
        SurveyDocument surveyDocument = restAPIService.getSurveyDocument(surveyDocumentId);

        SurveyDetailDto surveyDetailDto = new SurveyDetailDto();
        QuestionDetailDto reliabilityQuestionDto = new QuestionDetailDto();
        DesignResponseDto designResponseDto = new DesignResponseDto();

        ReliabilityQuestion reliabilityQuestion = null;
        List<ChoiceDetailDto> reliabiltyChoiceDtos = new ArrayList<>();

        if(surveyDocument.getReliability()){
            try {
                Long l = Long.valueOf(-1);
                reliabilityQuestion = reliabilityQuestion();
                reliabilityQuestionDto.setId(l);
                reliabilityQuestionDto.setTitle(reliabilityQuestion.getTitle());
                reliabilityQuestionDto.setQuestionType(reliabilityQuestion.getType());
                List<ReliabilityChoice> Rchoices = reliabilityQuestion.getChoiceList();
                for (ReliabilityChoice choice : Rchoices) {
                    ChoiceDetailDto choiceDto = new ChoiceDetailDto();
                    choiceDto.setId(l);
                    choiceDto.setTitle(choice.getChoiceName());
                    choiceDto.setCount(0);
                    reliabiltyChoiceDtos.add(choiceDto);
                }
                reliabilityQuestionDto.setChoiceList(reliabiltyChoiceDtos);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        // SurveyDocument에서 SurveyParticipateDto로 데이터 복사
        surveyDetailDto.setId(surveyDocument.getId());
        surveyDetailDto.setTitle(surveyDocument.getTitle());
        surveyDetailDto.setDescription(surveyDocument.getDescription());

        // 디자인
        designResponseDto.setFont(surveyDocument.getDesign().getFont());
        designResponseDto.setFontSize(surveyDocument.getDesign().getFontSize());
        designResponseDto.setBackColor(surveyDocument.getDesign().getBackColor());
        surveyDetailDto.setDesign(designResponseDto);

        // 날짜
        surveyDetailDto.setStartDate(surveyDocument.getDate().getStartDate());
        surveyDetailDto.setEndDate(surveyDocument.getDate().getDeadline());
        surveyDetailDto.setEnable(surveyDocument.getDate().isEnabled());

        // 진정성
        surveyDetailDto.setReliability(surveyDocument.getReliability());

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
                List<QuestionAnswer> questionAnswersByCheckAnswerId = questionAnswerRepository.findQuestionAnswersByCheckAnswerId(questionDocument.getId());
                for (QuestionAnswer questionAnswer : questionAnswersByCheckAnswerId) {
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
            if (questionDocument.getWordCloudList() != null) {
                for (WordCloud wordCloud : questionDocument.getWordCloudList()) {
                    WordCloudDto wordCloudDto = new WordCloudDto();
                    wordCloudDto.setId(wordCloud.getId());
                    wordCloudDto.setTitle(wordCloud.getTitle());
                    wordCloudDto.setCount(wordCloud.getCount());

                    wordCloudDtos.add(wordCloudDto);
                }
            }
            questionDto.setWordCloudDtos(wordCloudDtos);

            questionDtos.add(questionDto);
        }
        if(surveyDocument.getReliability()) {
            reliabilityquestionNumber=random.nextInt(questionDtos.size());
            questionDtos.add(reliabilityquestionNumber, reliabilityQuestionDto);
        }
        surveyDetailDto.setQuestionList(questionDtos);

        log.info(String.valueOf(surveyDetailDto));
        return surveyDetailDto;
    }

    public List<QuestionAnswer> getQuestionAnswerByCheckAnswerId(Long id) {
        return questionAnswerRepository.findQuestionAnswersByCheckAnswerId(id);
    }

    public List<SurveyAnswer> getSurveyAnswersBySurveyDocumentId(Long id) {
        return surveyAnswerRepository.findSurveyAnswersBySurveyDocumentId(id);
    }
}
