package com.example.surveyanalyze.survey.service;

import com.example.surveyanalyze.survey.domain.*;
import com.example.surveyanalyze.survey.exception.InvalidPythonException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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

    // 파이썬에 DocumentId 보내주고 분석결과 Entity에 매핑해서 저장
    public void analyze(String stringId) throws InvalidPythonException {
        long surveyDocumentId = Long.parseLong(stringId);

        try {
            String line = "[[['1', [0.6666666666666666, '3'], [0.3333333333333333, '4']], ['2', [0.6666666666666666, '4'], [0.3333333333333333, '3']], ['3', [0.6666666666666666, '1'], [0.3333333333333333, '2']], ['4', [0.6666666666666666, '2'], [0.3333333333333333, '1']]], [[[1.0], [1.0]], [[1.0], [1.0]]], [[0.10247043485974942, 1.0], [1.0, 0.10247043485974942]]]";
            line = getAnalyzeResult(surveyDocumentId);
            // for test
            if (surveyDocumentId == -1) {
                line = getAnalyzeResult(surveyDocumentId);
            }
            List<Object> testList = getListResult(line);
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

    // 분석 상세 분석 Get
    public SurveyAnalyzeDto readSurveyDetailAnalyze(Long surveyDocumentId) {
        //Survey_Id를 가져와서 그 Survey 의 상세분석을 가져옴
        Optional<SurveyAnalyze> surveyAnalyze = surveyAnalyzeRepository.findBySurveyDocumentId(surveyDocumentId);

        return surveyAnalyze.map(this::getSurveyDetailAnalyzeDto).orElse(null);
    }


//    private static String removeStopwords(List<String> inputList, List<String> stopwords) {
//        List<String> filteredList = new ArrayList<>();
//
//        for (String inputString : inputList) {
//            // Tokenize the input string
//            String[] words = StringUtils.tokenizeToStringArray(inputString, " ");
//
//            // Remove stopwords
//            List<String> filteredWords = new ArrayList<>();
//            for (String word : words) {
//                // Convert word to lowercase for case-insensitive matching
//                String lowercaseWord = word.toLowerCase();
//
//                // Skip stopwords
//                if (!contains(new List[]{stopwords}, lowercaseWord)) {
//                    filteredWords.add(word);
//                }
//            }
//
//            // Reconstruct the filtered string
//            String filteredString = StringUtils.arrayToDelimitedString(filteredWords.toArray(), " ");
//            filteredList.add(filteredString);
//        }
//
//        return filteredList.toString().trim();
//    }

//    private static Map<String, Integer> countWords(String text) {
//        String[] words = text.split("\\s+");
//        Map<String, Integer> wordCount = new HashMap<>();
//
//        for (String word : words) {
//            if (!word.isEmpty()) {
//                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
//            }
//        }
//        return wordCount;
//    }

    // SurveyDetailDto Response 보낼 SurveyDetailDto로 변환하는 메서드
    public SurveyDetailDto getSurveyDetailDto(Long surveyDocumentId) {
        SurveyDetailDto surveyDetailDto = restAPIService.getSurveyDetailDto(surveyDocumentId);

        List<QuestionDetailDto> questionDtos = surveyDetailDto.getQuestionList();
        for (QuestionDetailDto questionDetailDto : questionDtos) {
            // question type에 따라 choiceDto 에 들어갈 내용 구분
            // 주관식이면 choiceDto title에 주관식 응답을 저장??
            // 객관식 찬부식 -> 기존 방식 과 똑같이 count를 올려서 저장
            if (questionDetailDto.getQuestionType() == 0) {
                // 주관식 답변들 리스트
                List<QuestionAnswerDto> questionAnswersByCheckAnswerId = restAPIService.getQuestionAnswerByCheckAnswerId(questionDetailDto.getId());
                for (QuestionAnswerDto questionAnswer : questionAnswersByCheckAnswerId) {
                    ChoiceDetailDto choiceDto = new ChoiceDetailDto();
                    choiceDto.setId(questionAnswer.getId());
                    choiceDto.setTitle(questionAnswer.getCheckAnswer());
                    choiceDto.setCount(0);

                    questionDetailDto.getChoiceList().add(choiceDto);
                }
            }
        }
        return surveyDetailDto;
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
                    if ((errorLine = errorReader.readLine()) == null) break;
                } catch (IOException e) {
                    throw new InvalidPythonException(e);
                }
                System.out.println(errorLine);
            }
        }

        System.out.println("Process exited with code " + exitCode);
    }

    private static List<Object> getListResult(String line) throws JsonProcessingException {
        String inputString = line.replaceAll("'", "");

        log.info("result python");
        log.info(inputString);

        ObjectMapper objectMapper = new ObjectMapper();
        List<Object> testList = objectMapper.readValue(inputString, List.class);
        log.info(String.valueOf(testList));
        return testList;
    }

    private void saveSurveyAnalyze(long surveyDocumentId, ArrayList<Object> apriori, ArrayList<Object> compare, ArrayList<Object> chi) {
        SurveyAnalyze surveyAnalyze = getSurveyAnalyze(surveyDocumentId);

        //get surveyDetailDto
        SurveyDetailDto surveyDetailDto = restAPIService.getSurveyDetailDto(surveyDocumentId);

        saveQuestionAnalyze(apriori, compare, chi, surveyAnalyze, surveyDetailDto);
        surveyAnalyzeRepository.flush();
    }

    private void saveQuestionAnalyze(ArrayList<Object> apriori, ArrayList<Object> compare, ArrayList<Object> chi, SurveyAnalyze surveyAnalyze, SurveyDetailDto surveyDetailDto) {
        int p = 0;
        for (QuestionDetailDto questionDetailDto : surveyDetailDto.getQuestionList()) {
            if (questionDetailDto.getQuestionType() == 0) {
                continue;
            }
            QuestionAnalyze questionAnalyze;
            questionAnalyze = QuestionAnalyze.builder()
                    .questionTitle(questionDetailDto.getTitle())
                    .surveyAnalyze(surveyAnalyze)
                    .build();

            // 위치 변경 요망
            questionAnalyzeRepository.save(questionAnalyze);

            // compare
            List<Object> compareList = (List<Object>) compare.get(p);
            List<QuestionDetailDto> questionDocumentList = surveyDetailDto.getQuestionList();
            saveCompare(questionAnalyze, compareList, questionDocumentList, questionDocumentList.size());

            // chi
            List<Object> chiList = (List<Object>) chi.get(p);
            saveChi(questionAnalyze, questionDocumentList, questionDocumentList.size(), chiList);


            questionAnalyzeRepository.flush();
            p++;
        }
        // apriori
        // Define a custom Comparator
        log.info(String.valueOf(apriori));
        saveApriori(apriori, surveyAnalyze);
        surveyAnalyzeRepository.flush();
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
//        Optional<SurveyDetailDto> findSurvey = surveyDocumentRepository.findById(surveyId);
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
//        Optional<SurveyDetailDto> optionalSurvey = surveyDocumentRepository.findById(surveyId);
//
//        if (optionalSurvey.isPresent()) {
//            SurveyDetailDto surveyDetailDto = optionalSurvey.get();
//            // update survey properties using the manage DTO
//            surveyDetailDto.setDeadline(manage.getDeadline());
//            surveyDetailDto.setUrl(manage.getUrl());
//            surveyDetailDto.setStartDate(manage.getStartDate());
//            surveyDetailDto.setAcceptResponse(manage.isAcceptResponse());
//
//            surveyDocumentRepository.save(surveyDetailDto);
//        } else {
//            throw new InvalidSurveyException();
//        }
////        checkInvalidToken(request);
//    }

    private void saveApriori(ArrayList<Object> apriori, SurveyAnalyze surveyAnalyze) {
        //apriori
        List<AprioriAnalyze> aprioriAnalyzeList = new ArrayList<>();
        for (Object o : apriori) {
            List<Object> dataList = (List<Object>) o;
            Long choiceId = Long.valueOf((Integer) dataList.get(0));
            AprioriAnalyze aprioriAnalyze;
            ChoiceDetailDto choiceDto = restAPIService.getChoiceDto(choiceId);
            QuestionDetailDto questionDocument1 = restAPIService.getQuestionByChoiceId(choiceId);
            aprioriAnalyze = AprioriAnalyze.builder()
                    .choiceId(choiceId)
                    .choiceTitle(choiceDto.getTitle())
                    .questionTitle(questionDocument1.getTitle())
                    .choiceAnalyzeList(new ArrayList<>())
                    .surveyAnalyze(surveyAnalyze)
                    .build();

            aprioriAnalyzeRepository.save(aprioriAnalyze);
            saveAprioriSecond(dataList, aprioriAnalyze, questionDocument1);
            aprioriAnalyzeList.add(aprioriAnalyze);
            aprioriAnalyzeRepository.flush();
        }
        // Define a custom Comparator
        Comparator<AprioriAnalyze> choiceIdComparator = Comparator.comparing(AprioriAnalyze::getChoiceId);

        // Sort the list using the custom Comparator
        aprioriAnalyzeList.sort(choiceIdComparator);
        surveyAnalyzeRepository.flush();
    }

    private void saveAprioriSecond(List<Object> dataList, AprioriAnalyze aprioriAnalyze, QuestionDetailDto questionDocument) {
        for (int i = 0; i < dataList.size() - 1; i++) {
            List<Object> subList = (List<Object>) dataList.get(i + 1);
            double support = Math.round((double) subList.get(0) * 1000) / 1000.0;
            Long choiceId2 = Long.valueOf((Integer) subList.get(1));
            ChoiceDetailDto choice1 = restAPIService.getChoiceDto(choiceId2);
            ChoiceAnalyze choiceAnalyze = ChoiceAnalyze.builder()
                    .choiceTitle(choice1.getTitle())
                    .support(support)
                    .aprioriAnalyze(aprioriAnalyze)
                    .choiceId(choiceId2)
                    .questionTitle(questionDocument.getTitle())
                    .build();
            choiceAnalyzeRepository.save(choiceAnalyze);
        }
        aprioriAnalyzeRepository.flush();
    }

    private void saveChi(QuestionAnalyze questionAnalyze, List<QuestionDetailDto> questionDocumentList, int size, List<Object> chiList) {
        int o = 0;
        for (int k = 0; k < size; k++) {
            if (questionDocumentList.get(k).getQuestionType() == 0) {
                continue;
            }
            if (Objects.equals(questionDocumentList.get(k).getTitle(), questionAnalyze.getQuestionTitle())) {
                continue;
            }
            Double pValue = (Double) chiList.get(o);
            ChiAnalyze chiAnalyze = ChiAnalyze.builder()
                    .questionAnalyze(questionAnalyze)
                    .pValue(pValue)
                    .questionTitle(questionDocumentList.get(k).getTitle())
                    .build();
            o++;
            chiAnalyzeRepository.save(chiAnalyze);
        }
        questionAnalyzeRepository.flush();
    }

    private void saveCompare(QuestionAnalyze questionAnalyze, List<Object> compareList, List<QuestionDetailDto> questionList, int size) {
        int o = 0;
        for (int k = 0; k < size; k++) {
            if (questionList.get(k).getQuestionType() == 0) {
                continue;
            }
            if (Objects.equals(questionList.get(k).getTitle(), questionAnalyze.getQuestionTitle())) {
                continue;
            }
            ArrayList<Double> temp = (ArrayList<Double>) compareList.get(o);
            Double pValue = temp.get(0); // Assuming you want to retrieve the first Double value from the ArrayList

            CompareAnalyze compareAnalyze = CompareAnalyze.builder()
                    .questionAnalyze(questionAnalyze)
                    .pValue(pValue)
                    .questionTitle(questionList.get(k).getTitle())
                    .build();
            o++;
            compareAnalyzeRepository.save(compareAnalyze);
        }
        questionAnalyzeRepository.flush();
    }

    private SurveyAnalyze getSurveyAnalyze(long surveyDocumentId) {
        // 값 분리해서 Analyze DB에 저장
        Optional<SurveyAnalyze> surveyAnalyzeOp = surveyAnalyzeRepository.findBySurveyDocumentId(surveyDocumentId);
        if (surveyAnalyzeOp.isPresent()) {
            surveyAnalyzeRepository.deleteAllBySurveyDocumentId(surveyDocumentId);
            SurveyAnalyze newSurveyAnalyze = SurveyAnalyze.builder().build();
            surveyAnalyzeRepository.save(newSurveyAnalyze);
            return newSurveyAnalyze;
        } else {
            SurveyAnalyze surveyAnalyze = SurveyAnalyze.builder()
                    .surveyDocumentId(surveyDocumentId)
                    .questionAnalyzeList(new ArrayList<>())
                    .build();
            surveyAnalyzeRepository.save(surveyAnalyze);
            return surveyAnalyze;
        }
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

        return br.readLine();
    }

    //    @Transactional
//    public void wordCloud(String stringId) {
//        long surveyDocumentId = Long.parseLong(stringId);
//        // 값 분리해서 Analyze DB에 저장
//        SurveyDetailDto surveyDetailDto = restAPIService.getSurveyDetailDto(surveyDocumentId);
//        List<QuestionDetailDto> questionDocumentList = surveyDetailDto.getQuestionList();
//        for (QuestionDetailDto questionDetailDto : questionDocumentList) {
//            if (questionDetailDto.getQuestionType() != 0) {
//                continue;
//            }
//            // 주관식 문항의 id로 그 주관식 문항에 대답한 questionAnswerList를 찾아옴
//            // get questionAnswers By CheckAnswerId
//            List<QuestionAnswerDto> questionAnswersByCheckAnswerId = restAPIService.getQuestionAnswerByCheckAnswerId(questionDetailDto.getId());
////            List<QuestionAnswerDto> questionAnswersByCheckAnswerId = questionAnswerRepository.findQuestionAnswersByCheckAnswerId(questionDetailDto.getId());
//
//            //wordCloud 분석
//            ArrayList<String> answerList = new ArrayList<>();
//            for (QuestionAnswerDto questionAnswer : questionAnswersByCheckAnswerId) {
//                if (questionAnswer.getQuestionType() != 0) {
//                    continue;
//                }
//                answerList.add(questionAnswer.getCheckAnswer());
//            }
//            log.info(String.valueOf(answerList));
//
//            Resource[] resources = new Resource[0];
//            try {
//                resources = ResourcePatternUtils
//                        .getResourcePatternResolver(new DefaultResourceLoader())
//                        .getResources("classpath*:python/stopword.txt");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            String resourceFolderLocation = resourceLocator.getResourceFolderLocation();
//
////            log.info(String.valueOf(resources[0]));
////            String substring = String.valueOf(resources[0]).substring(6, String.valueOf(resources[0]).length() -1);
//            log.info(resourceFolderLocation);
//
//            List<String> stopwords = new ArrayList<>();
//
//            try (BufferedReader reader = new BufferedReader(new FileReader(resourceFolderLocation + "/python/python4.py"))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    stopwords.add(line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            String filterWords = removeStopwords(answerList, stopwords);
//
//            for (String s : Arrays.asList("\\[", "\\]", ",", "'")) {
//                filterWords = filterWords.replaceAll(s, "");
//            }
//            log.info(filterWords);
//
//            Map<String, Integer> wordCount = countWords(filterWords);
//            // Sort the wordCount map in descending order of values
//            List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(wordCount.entrySet());
//            sortedList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
//
//            // Print the sorted word counts
//            log.info("Word Counts (Descending Order):");
//            List<WordCloudDto> wordCloudList = new ArrayList<>();
//            for (Map.Entry<String, Integer> entry : sortedList) {
//                WordCloudDto wordCloud = new WordCloudDto();
//                wordCloud.setTitle(entry.getKey());
//                wordCloud.setCount(entry.getValue());
//                log.info(entry.getKey() + ": " + entry.getValue());
//                wordCloudList.add(wordCloud);
//            }
//
//            List<WordCloudDto> wordCloudDtos = new ArrayList<>();
//            for (WordCloudDto wordCloud : wordCloudList) {
//                WordCloudDto wordCloudDto = new WordCloudDto();
//                wordCloudDto.setId(wordCloudDto.getId());
//                wordCloudDto.setTitle(wordCloud.getTitle());
//                wordCloudDto.setCount(wordCloud.getCount());
//                wordCloudDtos.add(wordCloudDto);
//            }
//
//            // post to questionDetailDto to set WordCloudList
//            Long id = questionDetailDto.getId();
//            restAPIService.postToQuestionToSetWordCloud(id, wordCloudDtos);
////            questionDetailDto.setWordCloudList(wordCloudList);
////            questionDocumentRepository.flush();
//        }
//    }

    private SurveyAnalyzeDto getSurveyDetailAnalyzeDto(SurveyAnalyze surveyAnalyze) {
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
            for (ChoiceAnalyze choiceDto : aprioriAnalyze.getChoiceAnalyzeList()) {
                ChoiceAnalyzeDto choiceAnalyzeDto = new ChoiceAnalyzeDto();
                choiceAnalyzeDto.setId(choiceDto.getId());
                choiceAnalyzeDto.setChoiceTitle(choiceDto.getChoiceTitle());
                choiceAnalyzeDto.setSupport(choiceDto.getSupport());
                choiceAnalyzeDto.setQuestionTitle(choiceDto.getQuestionTitle());
                choiceDtos.add(choiceAnalyzeDto);
            }
            aprioriAnalyzeDto.setChoiceAnalyzeList(choiceDtos);
            aprioriAnalyzeDtos.add(aprioriAnalyzeDto);
        }
        surveyAnalyzeDto.setAprioriAnalyzeList(aprioriAnalyzeDtos);
        surveyAnalyzeDto.setQuestionAnalyzeList(questionDtos);

        return surveyAnalyzeDto;
    }

}
