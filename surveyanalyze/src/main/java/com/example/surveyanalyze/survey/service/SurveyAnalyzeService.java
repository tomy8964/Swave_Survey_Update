package com.example.surveyanalyze.survey.service;

import com.example.surveyanalyze.ResourceLocator;
import com.example.surveyanalyze.survey.domain.*;
import com.example.surveyanalyze.survey.exception.InvalidPythonException;
import com.example.surveyanalyze.survey.exception.InvalidTokenException;
import com.example.surveyanalyze.survey.repository.aprioriAnlayze.AprioriAnalyzeRepository;
import com.example.surveyanalyze.survey.repository.chiAnlayze.ChiAnalyzeRepository;
import com.example.surveyanalyze.survey.repository.choiceAnalyze.ChoiceAnalyzeRepository;
import com.example.surveyanalyze.survey.repository.compareAnlayze.CompareAnalyzeRepository;
import com.example.surveyanalyze.survey.repository.questionAnlayze.QuestionAnalyzeRepository;
import com.example.surveyanalyze.survey.repository.surveyAnalyze.SurveyAnalyzeRepository;
import com.example.surveyanalyze.survey.response.*;
import com.example.surveyanalyze.survey.restAPI.service.RestAPIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.thymeleaf.util.ArrayUtils.contains;

//import static com.example.surveyAnalyze.util.SurveyTypeCheck.typeCheck;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyAnalyzeService {
    private final ChiAnalyzeRepository chiAnalyzeRepository;
    private final CompareAnalyzeRepository compareAnalyzeRepository;
    private final AprioriAnalyzeRepository aprioriAnalyzeRepository;
    private final ChoiceAnalyzeRepository choiceAnalyzeRepository;
    private final QuestionAnalyzeRepository questionAnalyzeRepository;
    private final SurveyAnalyzeRepository surveyAnalyzeRepository;
    private final RestAPIService restAPIService;
    private final ResourceLocator resourceLocator;

    private static List<Object> getListResult(String line) throws JsonProcessingException {
        String inputString = line.replaceAll("'", "");
//            String inputString = testString.replaceAll("'", "");

        log.info("result python");
        log.info(inputString);

        ObjectMapper objectMapper = new ObjectMapper();
        List<Object> testList = objectMapper.readValue(inputString, List.class);
        log.info(String.valueOf(testList));
        return testList;
    }

    // 회원 유효성 검사, token 존재하지 않으면 예외처리
    private static void checkInvalidToken(HttpServletRequest request) throws InvalidTokenException {
        if (request.getHeader("Authorization") == null) {
            log.info("error");
            throw new InvalidTokenException();
        }
        log.info("토큰 체크 완료");
    }

    private static String removeStopwords(List<String> inputList, List<String> stopwords) {
        List<String> filteredList = new ArrayList<>();

        for (String inputString : inputList) {
            // Tokenize the input string
            String[] words = StringUtils.tokenizeToStringArray(inputString, " ");

            // Remove stopwords
            List<String> filteredWords = new ArrayList<>();
            for (String word : words) {
                // Convert word to lowercase for case-insensitive matching
                String lowercaseWord = word.toLowerCase();

                // Skip stopwords
                if (!contains(new List[]{stopwords}, lowercaseWord)) {
                    filteredWords.add(word);
                }
            }

            // Reconstruct the filtered string
            String filteredString = StringUtils.arrayToDelimitedString(filteredWords.toArray(), " ");
            filteredList.add(filteredString);
        }

        return filteredList.toString().trim();
    }

    public static Map<String, Integer> countWords(String text) {
        String[] words = text.split("\\s+");
        Map<String, Integer> wordCount = new HashMap<>();

        for (String word : words) {
            if (!word.isEmpty()) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        return wordCount;
    }

    // 파이썬에 DocumentId 보내주고 분석결과 Entity에 매핑해서 저장
    @Transactional
    public void analyze(String stringId) throws InvalidPythonException {
        long surveyDocumentId = Long.parseLong(stringId);

        try {
            String testString = "[[['1', [0.6666666666666666, '3'], [0.3333333333333333, '4']], ['2', [0.6666666666666666, '4'], [0.3333333333333333, '3']], ['3', [0.6666666666666666, '1'], [0.3333333333333333, '2']], ['4', [0.6666666666666666, '2'], [0.3333333333333333, '1']]], [[[1.0], [1.0]], [[1.0], [1.0]]], [[0.10247043485974942, 1.0], [1.0, 0.10247043485974942]]]";
            String line;
            // for test
            if (surveyDocumentId == -1) {
                line = testString;
            } else {
                line = getAnalyzeResult(surveyDocumentId);
//                line = testString;
            }
            List<Object> testList = getListResult(line);
/*
[testList
    [
        [
            'ex)1'[[1.0, '0_1']], [[1.0, '0_2']]
        ],
        [
            'ex)2'[[1.0, '1_3']], [[1.0, '1_4']]
        ]
    ],
    [
        [[0.8075499102701248], [0.42264973081037427]], [[0.42264973081037427], [0.8075499102701248]]
    ],
    [
        [0.1921064408679386, 1.0], [1.0, 0.1921064408679386]
    ]
]
*/
            ArrayList<Object> apriori = (ArrayList<Object>) testList.get(0);
            ArrayList<Object> compare = (ArrayList<Object>) testList.get(1);
            ArrayList<Object> chi = (ArrayList<Object>) testList.get(2);

            saveSurveyAnalyze(surveyDocumentId, apriori, compare, chi);
        } catch (IOException e) {
            // 체크 예외 -> 런타임 커스텀 예외 변환 처리
            // python 파일 오류
            throw new InvalidPythonException(e);
        }
    }

    private void saveSurveyAnalyze(long surveyDocumentId, ArrayList<Object> apriori, ArrayList<Object> compare, ArrayList<Object> chi) {
        SurveyAnalyze surveyAnalyze = getSurveyAnalyze(surveyDocumentId);

        //get surveyDocument
        SurveyDocument surveyDocument = restAPIService.getSurveyDocument(surveyDocumentId);

        saveQuestionAnalyze(apriori, compare, chi, surveyAnalyze, surveyDocument);
        surveyAnalyzeRepository.flush();
    }

    private void saveQuestionAnalyze(ArrayList<Object> apriori, ArrayList<Object> compare, ArrayList<Object> chi, SurveyAnalyze surveyAnalyze, SurveyDocument surveyDocument) {
        int p = 0;
        List<QuestionAnalyze> questionAnalyzeList = new ArrayList<>();
        for (QuestionDocument questionDocument : surveyDocument.getQuestionDocumentList()) {
            if (questionDocument.getQuestionType() == 0) {
                continue;
            }
            QuestionAnalyze questionAnalyze;
            questionAnalyze = QuestionAnalyze.builder()
                    .questionTitle(questionDocument.getTitle())
                    .surveyAnalyzeId(surveyAnalyze)
                    .build();

            questionAnalyzeRepository.save(questionAnalyze);

            // compare
            List<Object> compareList = (List<Object>) compare.get(p);
            List<QuestionDocument> questionDocumentList = surveyDocument.getQuestionDocumentList();
            saveCompare(questionAnalyze, compareList, questionDocumentList, questionDocumentList.size());

            // chi
            List<Object> chiList = (List<Object>) chi.get(p);
            saveChi(questionAnalyze, questionDocumentList, questionDocumentList.size(), 0, chiList);


            questionAnalyzeRepository.flush();
            p++;
            questionAnalyzeList.add(questionAnalyze);
        }
        // apriori
        // Define a custom Comparator
        log.info(String.valueOf(apriori));
        saveApriori(apriori, surveyAnalyze);
        surveyAnalyze.setQuestionAnalyzeList(questionAnalyzeList);
        surveyAnalyzeRepository.flush();
    }

    private void saveApriori(ArrayList<Object> apriori, SurveyAnalyze surveyAnalyze) {
        //apriori
        List<AprioriAnalyze> aprioriAnalyzeList = new ArrayList<>();
        for (int j = 0; j < apriori.size(); j++) {
            // [['1', [0.66, '3'], [0.33, '4']]
            List<Object> dataList = (List<Object>) apriori.get(j);
            Long choiceId = Long.valueOf((Integer) dataList.get(0));
            AprioriAnalyze aprioriAnalyze;
            //get Choice
            Choice choice = restAPIService.getChoice(choiceId);
            QuestionDocument questionDocument1 = restAPIService.getQuestionByChoiceId(choiceId);
//                    QuestionDocument questionDocument1 = getQuestionDocument(choice.getQuestion_id().getId());
            aprioriAnalyze = AprioriAnalyze.builder()
                    .choiceId(choiceId)
                    .choiceTitle(choice.getTitle())
                    .questionTitle(questionDocument1.getTitle())
                    .choiceAnalyzeList(new ArrayList<>())
                    .surveyAnalyzeId(surveyAnalyze)
                    .build();

            aprioriAnalyzeRepository.save(aprioriAnalyze);
            // for문 [0.88,2] 같은 배열의 갯수 만큼
            // [[0.88,3],[0.8,5]]
            saveAprioriSecond(dataList, aprioriAnalyze, questionDocument1);
            aprioriAnalyzeList.add(aprioriAnalyze);
            aprioriAnalyzeRepository.flush();
        }
        // Define a custom Comparator
        Comparator<AprioriAnalyze> choiceIdComparator = Comparator.comparing(AprioriAnalyze::getChoiceId);

        // Sort the list using the custom Comparator
        Collections.sort(aprioriAnalyzeList, choiceIdComparator);
        surveyAnalyze.setAprioriAnalyzeList(aprioriAnalyzeList);
        surveyAnalyzeRepository.flush();
    }

    private void saveAprioriSecond(List<Object> dataList, AprioriAnalyze aprioriAnalyze, QuestionDocument questionDocument1) {
        List<ChoiceAnalyze> choiceAnalyzeList = new ArrayList<>();
        for (int i = 0; i < dataList.size() - 1; i++) {
            List<Object> subList = (List<Object>) dataList.get(i + 1);
            ChoiceAnalyze choiceAnalyze = new ChoiceAnalyze();
            double support = Math.round((double) subList.get(0) * 1000) / 1000.0;
            Long choiceId2 = Long.valueOf((Integer) subList.get(1));
            Choice choice1 = restAPIService.getChoice(choiceId2);
            choiceAnalyze = choiceAnalyze.builder()
                    .choiceTitle(choice1.getTitle())
                    .support(support)
                    .aprioriAnalyzeId(aprioriAnalyze)
                    .choiceId(choiceId2)
                    .questionTitle(questionDocument1.getTitle())
                    .build();
            choiceAnalyzeRepository.save(choiceAnalyze);
            choiceAnalyzeList.add(choiceAnalyze);
        }
        aprioriAnalyze.setChoiceAnalyzeList(choiceAnalyzeList);
        aprioriAnalyzeRepository.flush();
    }

    private void saveChi(QuestionAnalyze questionAnalyze, List<QuestionDocument> questionDocumentList, int size, int o, List<Object> chiList) {
        List<ChiAnalyze> chiAnalyzeList = new ArrayList<>();
        for (int k = 0; k < size; k++) {
            if (questionDocumentList.get(k).getQuestionType() == 0) {
                continue;
            }
            if (questionDocumentList.get(k).getTitle() == questionAnalyze.getQuestionTitle()) {
                continue;
            }
            Double pValue = (Double) chiList.get(o);
            ChiAnalyze chiAnalyze = new ChiAnalyze();
            chiAnalyze = ChiAnalyze.builder()
                    .questionAnalyzeId(questionAnalyze)
                    .pValue(pValue)
                    .questionTitle(questionDocumentList.get(k).getTitle())
                    .build();
            o++;
            chiAnalyzeRepository.save(chiAnalyze);
            chiAnalyzeList.add(chiAnalyze);
        }
        questionAnalyze.setChiAnalyzeList(chiAnalyzeList);
        questionAnalyzeRepository.flush();
    }

    private void saveCompare(QuestionAnalyze questionAnalyze, List<Object> compareList, List<QuestionDocument> questionDocumentList, int size) {
        int o = 0;
        List<CompareAnalyze> compareAnalyzeList = new ArrayList<>();
        for (int k = 0; k < size; k++) {
            if (questionDocumentList.get(k).getQuestionType() == 0) {
                continue;
            }
            if (questionDocumentList.get(k).getTitle() == questionAnalyze.getQuestionTitle()) {
                continue;
            }
            ArrayList<Double> temp = (ArrayList<Double>) compareList.get(o);
            Double pValue = temp.get(0); // Assuming you want to retrieve the first Double value from the ArrayList

            CompareAnalyze compareAnalyze = CompareAnalyze.builder()
                    .questionAnalyzeId(questionAnalyze)
                    .pValue(pValue)
                    .questionTitle(questionDocumentList.get(k).getTitle())
                    .build();
            o++;
            compareAnalyzeRepository.save(compareAnalyze);
            compareAnalyzeList.add(compareAnalyze);
        }
        questionAnalyze.setCompareAnalyzeList(compareAnalyzeList);
        questionAnalyzeRepository.flush();
    }

    // 분석 응답 (문항 별 응답 수 불러오기) (Count)
//    public SurveyDetailDto readCountChoice(HttpServletRequest request, Long surveyId) throws InvalidTokenException {
////        checkInvalidToken(request);
//        return getSurveyDetailDto(surveyId);
//    }

    // 분석 관리 Get
//    public SurveyManageDto readSurveyMange(HttpServletRequest request, Long surveyId) throws InvalidTokenException {
////        checkInvalidToken(request);
//
//        //Survey_Id를 가져와서 그 Survey 의 Document 를 가져옴
//        Optional<SurveyDocument> findSurvey = surveyDocumentRepository.findById(surveyId);
//
//        if (findSurvey.isPresent()) {
//            //manage 부분만 추출
//            SurveyManageDto manage = new SurveyManageDto();
//            manage.builder()
//                    .acceptResponse(findSurvey.get().isAcceptResponse())
//                    .startDate(findSurvey.get().getStartDate())
//                    .deadline(findSurvey.get().getDeadline())
//                    .url(findSurvey.get().getUrl())
//                    .build();
//
//            return manage;
//        }else {
//            throw new InvalidSurveyException();
//        }
//    }

    // 분석 관리 Post
//    public void setSurveyMange(HttpServletRequest request, Long surveyId, SurveyManageDto manage) throws InvalidTokenException {
////        checkInvalidToken(request);
//        Optional<SurveyDocument> optionalSurvey = surveyDocumentRepository.findById(surveyId);
//
//        if (optionalSurvey.isPresent()) {
//            SurveyDocument surveyDocument = optionalSurvey.get();
//            // update survey properties using the manage DTO
//            surveyDocument.setDeadline(manage.getDeadline());
//            surveyDocument.setUrl(manage.getUrl());
//            surveyDocument.setStartDate(manage.getStartDate());
//            surveyDocument.setAcceptResponse(manage.isAcceptResponse());
//
//            surveyDocumentRepository.save(surveyDocument);
//        } else {
//            throw new InvalidSurveyException();
//        }
////        checkInvalidToken(request);
//    }

    private SurveyAnalyze getSurveyAnalyze(long surveyDocumentId) {
        // 값 분리해서 Analyze DB에 저장
        SurveyAnalyze surveyAnalyze;
//        if (surveyDocumentId == -1) {
//            surveyAnalyze = surveyAnalyzeRepository.findById(1L).get();
//        } else {
        surveyAnalyze = surveyAnalyzeRepository.findBySurveyDocumentId(surveyDocumentId);
//        }
        // 과거의 분석 결과 있으면 questionAnalyze delete & null 주입
        if (surveyAnalyze != null) {
            questionAnalyzeRepository.deleteAllBySurveyAnalyzeId(surveyAnalyze);
            aprioriAnalyzeRepository.deleteAllBySurveyAnalyzeId(surveyAnalyze);
            surveyAnalyze.setAprioriAnalyzeList(new ArrayList<>());
            surveyAnalyze.setQuestionAnalyzeList(new ArrayList<>());
        } else {
            surveyAnalyze = SurveyAnalyze.builder()
                    .surveyDocumentId(surveyDocumentId)
                    .questionAnalyzeList(new ArrayList<>())
                    .build();
        }
        surveyAnalyzeRepository.save(surveyAnalyze);
        return surveyAnalyze;
    }

    private String getAnalyzeResult(long surveyDocumentId) throws IOException {
        System.out.println("pythonbuilder 시작");
        ProcessBuilder builder;

        Resource[] resources = ResourcePatternUtils
                .getResourcePatternResolver(new DefaultResourceLoader())
                .getResources("classpath*:python/python4.py");

        log.info(String.valueOf(resources[0]));
        //server 5 local 6
        String substring = String.valueOf(resources[0]).substring(6, String.valueOf(resources[0]).length() - 1);
        log.info(substring);
//        String resourceFolderLocation = resourceLocator.getResourceFolderLocation();

        builder = new ProcessBuilder("python", substring, String.valueOf(surveyDocumentId));

        builder.redirectErrorStream(true);
        Process process = builder.start();

        // 자식 프로세스가 종료될 때까지 기다림
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            // Handle interrupted exception
            exitCode = -1;
        }

        if (exitCode != 0) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            System.out.println("Error output:");
            while ((errorLine = errorReader.readLine()) != null) {
                System.out.println(errorLine);
            }
        }

        System.out.println("Process exited with code " + exitCode);

        //// 서브 프로세스가 출력하는 내용을 받기 위해
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line = br.readLine();
        return line;
    }

    //    @Transactional
    public void wordCloud(String stringId) {
        long surveyDocumentId = Long.parseLong(stringId);
        // 값 분리해서 Analyze DB에 저장
        SurveyDocument surveyDocument = restAPIService.getSurveyDocument(surveyDocumentId);
        List<QuestionDocument> questionDocumentList = surveyDocument.getQuestionDocumentList();
        for (QuestionDocument questionDocument : questionDocumentList) {
            if (questionDocument.getQuestionType() != 0) {
                continue;
            }
            // 주관식 문항의 id로 그 주관식 문항에 대답한 questionAnswerList를 찾아옴
            // get questionAnswers By CheckAnswerId
            List<QuestionAnswer> questionAnswersByCheckAnswerId = restAPIService.getQuestionAnswerByCheckAnswerId(questionDocument.getId());
//            List<QuestionAnswer> questionAnswersByCheckAnswerId = questionAnswerRepository.findQuestionAnswersByCheckAnswerId(questionDocument.getId());

            //wordCloud 분석
            ArrayList<String> answerList = new ArrayList<>();
            for (QuestionAnswer questionAnswer : questionAnswersByCheckAnswerId) {
                if (questionAnswer.getQuestionType() != 0) {
                    continue;
                }
                answerList.add(questionAnswer.getCheckAnswer());
            }
            log.info(String.valueOf(answerList));

            Resource[] resources = new Resource[0];
            try {
                resources = ResourcePatternUtils
                        .getResourcePatternResolver(new DefaultResourceLoader())
                        .getResources("classpath*:python/stopword.txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String resourceFolderLocation = resourceLocator.getResourceFolderLocation();

//            log.info(String.valueOf(resources[0]));
//            String substring = String.valueOf(resources[0]).substring(6, String.valueOf(resources[0]).length() -1);
            log.info(resourceFolderLocation);

            List<String> stopwords = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(resourceFolderLocation + "/python/python4.py"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stopwords.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String filterWords = removeStopwords(answerList, stopwords);

            for (String s : Arrays.asList("\\[", "\\]", ",", "'")) {
                filterWords = filterWords.replaceAll(s, "");
            }
            log.info(filterWords);

            Map<String, Integer> wordCount = countWords(filterWords);
            // Sort the wordCount map in descending order of values
            List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(wordCount.entrySet());
            sortedList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            // Print the sorted word counts
            log.info("Word Counts (Descending Order):");
            List<WordCloud> wordCloudList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : sortedList) {
                WordCloud wordCloud = new WordCloud();
                wordCloud.setQuestionDocument(questionDocument);
                wordCloud.setTitle(entry.getKey());
                wordCloud.setCount(entry.getValue());
                log.info(entry.getKey() + ": " + entry.getValue());
                wordCloudList.add(wordCloud);
            }

            List<WordCloudDto> wordCloudDtos = new ArrayList<>();
            for (WordCloud wordCloud : wordCloudList) {
                WordCloudDto wordCloudDto = new WordCloudDto();
                wordCloudDto.setId(wordCloudDto.getId());
                wordCloudDto.setTitle(wordCloud.getTitle());
                wordCloudDto.setCount(wordCloud.getCount());
                wordCloudDtos.add(wordCloudDto);
            }

            // post to questionDocument to set WordCloudList
            Long id = questionDocument.getId();
            restAPIService.postToQuestionToSetWordCloud(id, wordCloudDtos);
//            questionDocument.setWordCloudList(wordCloudList);
//            questionDocumentRepository.flush();
        }
    }

    // 분석 상세 분석 Get
    public SurveyAnalyzeDto readSurveyDetailAnalyze(Long surveyId) {
        //Survey_Id를 가져와서 그 Survey 의 상세분석을 가져옴
        SurveyAnalyze surveyAnalyze = surveyAnalyzeRepository.findBySurveyDocumentId(surveyId);

        if (surveyAnalyze == null) {
            return null;
        }
//        checkInvalidToken(request);
        return getSurveyDetailAnalyzeDto(surveyAnalyze.getId());
    }

    // SurveyDocument Response 보낼 SurveyDetailDto로 변환하는 메서드
    public SurveyDetailDto getSurveyDetailDto(Long surveyDocumentId) {
//        SurveyDocument surveyDocument = surveyDocumentRepository.findById(surveyDocumentId).get();
        SurveyDocument surveyDocument = restAPIService.getSurveyDocument(surveyDocumentId);

        SurveyDetailDto surveyDetailDto = new SurveyDetailDto();

        // SurveyDocument에서 SurveyParticipateDto로 데이터 복사
        surveyDetailDto.setId(surveyDocument.getId());
        surveyDetailDto.setTitle(surveyDocument.getTitle());
        surveyDetailDto.setDescription(surveyDocument.getDescription());
        surveyDetailDto.setCountAnswer(surveyDocument.getCountAnswer());

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
//                List<QuestionAnswer> questionAnswersByCheckAnswerId = questionAnswerRepository.findQuestionAnswersByCheckAnswerId(questionDocument.getId());
                List<QuestionAnswer> questionAnswersByCheckAnswerId = restAPIService.getQuestionAnswerByCheckAnswerId(questionDocument.getId());
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

    private SurveyAnalyzeDto getSurveyDetailAnalyzeDto(Long surveyId) {
        SurveyAnalyze surveyAnalyze = surveyAnalyzeRepository.findById(surveyId).get();
        SurveyAnalyzeDto surveyAnalyzeDto = new SurveyAnalyzeDto();

        // SurveyDocument에서 SurveyParticipateDto로 데이터 복사
        surveyAnalyzeDto.setId(surveyAnalyze.getId());

        List<QuestionAnalyzeDto> questionDtos = new ArrayList<>();
        for (QuestionAnalyze questionAnalyze : surveyAnalyze.getQuestionAnalyzeList()) {
            QuestionAnalyzeDto questionDto = new QuestionAnalyzeDto();
            questionDto.setId(questionAnalyze.getId());
            questionDto.setQuestionTitle(questionAnalyze.getQuestionTitle());

            List<CompareAnalyzeDto> compareAnalyzeDtos = new ArrayList<>();
            for (CompareAnalyze compareAnalyze : questionAnalyze.getCompareAnalyzeList()) {
                CompareAnalyzeDto compareAnalyzeDto = new CompareAnalyzeDto();
                compareAnalyzeDto.setId(compareAnalyze.getId());
                compareAnalyzeDto.setPValue(compareAnalyze.getPValue());
                compareAnalyzeDto.setQuestionTitle(compareAnalyze.getQuestionTitle());

                compareAnalyzeDtos.add(compareAnalyzeDto);
            }
            questionDto.setCompareAnalyzeList(compareAnalyzeDtos);

            List<ChiAnalyzeDto> chiAnalyzeDtos = new ArrayList<>();
            for (ChiAnalyze chiAnalyze : questionAnalyze.getChiAnalyzeList()) {
                ChiAnalyzeDto chiAnalyzeDto = new ChiAnalyzeDto();
                chiAnalyzeDto.setId(chiAnalyze.getId());
                chiAnalyzeDto.setPValue(chiAnalyze.getPValue());
                chiAnalyzeDto.setQuestionTitle(chiAnalyze.getQuestionTitle());

                chiAnalyzeDtos.add(chiAnalyzeDto);
            }
            questionDto.setChiAnalyzeList(chiAnalyzeDtos);

            questionDtos.add(questionDto);
        }
        List<AprioriAnalyzeDto> aprioriAnalyzeDtos = new ArrayList<>();
        for (AprioriAnalyze aprioriAnalyze : surveyAnalyze.getAprioriAnalyzeList()) {
            AprioriAnalyzeDto aprioriAnalyzeDto = new AprioriAnalyzeDto();
            aprioriAnalyzeDto.setId(aprioriAnalyze.getId());
            aprioriAnalyzeDto.setChoiceTitle(aprioriAnalyze.getChoiceTitle());
            aprioriAnalyzeDto.setQuestionTitle(aprioriAnalyze.getQuestionTitle());

            List<ChoiceAnalyzeDto> choiceDtos = new ArrayList<>();
            for (ChoiceAnalyze choice : aprioriAnalyze.getChoiceAnalyzeList()) {
                ChoiceAnalyzeDto choiceDto = new ChoiceAnalyzeDto();
                choiceDto.setId(choice.getId());
                choiceDto.setChoiceTitle(choice.getChoiceTitle());
                choiceDto.setSupport(choice.getSupport());
                choiceDto.setQuestionTitle(choice.getQuestionTitle());
                choiceDtos.add(choiceDto);
            }
            aprioriAnalyzeDto.setChoiceAnalyzeList(choiceDtos);
            aprioriAnalyzeDtos.add(aprioriAnalyzeDto);
        }
        surveyAnalyzeDto.setAprioriAnalyzeList(aprioriAnalyzeDtos);
        surveyAnalyzeDto.setQuestionAnalyzeList(questionDtos);

        return surveyAnalyzeDto;
    }

    public void wordCloudPython(String surveyDocumentId) {
        System.out.println("wordcloud pythonbuilder 시작");
        ProcessBuilder builder;

        Resource[] resources;
        try {
            resources = ResourcePatternUtils
                    .getResourcePatternResolver(new DefaultResourceLoader())
                    .getResources("classpath*:python/python5.py");
        } catch (IOException e) {
            throw new InvalidPythonException(e);
        }

        log.info(String.valueOf(resources[0]));
        String substring = String.valueOf(resources[0]).substring(6, String.valueOf(resources[0]).length() - 1);
        log.info(substring);
//        String resourceFolderLocation = resourceLocator.getResourceFolderLocation();

        builder = new ProcessBuilder("python", substring, surveyDocumentId);

        builder.redirectErrorStream(true);
        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new InvalidPythonException(e);
        }

        // 자식 프로세스가 종료될 때까지 기다림
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            // Handle interrupted exception
            exitCode = -1;
        }

        if (exitCode != 0) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            System.out.println("Error output:");
            while (true) {
                try {
                    if (!((errorLine = errorReader.readLine()) != null)) break;
                } catch (IOException e) {
                    throw new InvalidPythonException(e);
                }
                System.out.println(errorLine);
            }
        }

        System.out.println("Process exited with code " + exitCode);
    }
}
