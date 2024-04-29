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
import org.springframework.boot.json.JsonParseException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyAnalyzeService {
    private static final int APRIORI_NUMBER = 0;
    private static final int COMPARE_NUMBER = 1;
    private static final int CHI_NUMBER = 2;
    private final ChiAnalyzeRepository chiAnalyzeRepository;
    private final CompareAnalyzeRepository compareAnalyzeRepository;
    private final AprioriAnalyzeRepository aprioriAnalyzeRepository;
    private final ChoiceAnalyzeRepository choiceAnalyzeRepository;
    private final QuestionAnalyzeRepository questionAnalyzeRepository;
    private final SurveyAnalyzeRepository surveyAnalyzeRepository;
    private final RestAPIService restAPIService;

    // 파이썬에 DocumentId 보내주고 분석결과 Entity에 매핑해서 저장
    public void analyze(String stringId) {
        try {
            long surveyDocumentId = Long.parseLong(stringId);
            String line = getAnalyzeResult(surveyDocumentId);
            List<Object> resultList = getListResult(line);
            ArrayList<Object> apriori = (ArrayList<Object>) resultList.get(APRIORI_NUMBER);
            ArrayList<Object> compare = (ArrayList<Object>) resultList.get(COMPARE_NUMBER);
            ArrayList<Object> chi = (ArrayList<Object>) resultList.get(CHI_NUMBER);

            saveSurveyAnalyze(surveyDocumentId, apriori, compare, chi);
        } catch (IOException e) {
            throw new InvalidPythonException(e);
        }
    }

    private String getAnalyzeResult(long surveyDocumentId) throws IOException {
        String pythonLocation;
        try {
            Resource[] resources = ResourcePatternUtils
                    .getResourcePatternResolver(new DefaultResourceLoader())
                    .getResources("classpath*:python/python4.py");
            pythonLocation = String.valueOf(resources[0]).substring(6, String.valueOf(resources[0]).length() - 1);
            if (pythonLocation.isEmpty()) {
                throw new InvalidPythonException("올바르지 않은 파이썬 파일 경로입니다.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ProcessBuilder builder = new ProcessBuilder("python", pythonLocation, String.valueOf(surveyDocumentId));

        builder.redirectErrorStream(true);
        Process process = builder.start();

        // 자식 프로세스가 종료될 때까지 기다림
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            exitCode = -1;
        }

        if (exitCode != 0) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            log.error("Error output:");
            while ((errorLine = errorReader.readLine()) != null) {
                log.error(errorLine);
            }
        }

        log.error("Process exited with code " + exitCode);

        // 서브 프로세스가 출력하는 내용을 받기 위해
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        return br.readLine();
    }

    private List<Object> getListResult(String line) {
        String inputString = line.replaceAll("'", "");

        ObjectMapper objectMapper = new ObjectMapper();
        List<Object> resultList = null;
        try {
            resultList = objectMapper.readValue(inputString, List.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e);
        }
        return resultList;
    }

    // 분석 상세 분석 Get
    public SurveyAnalyzeDto readSurveyDetailAnalyze(Long surveyDocumentId) {
        //Survey_Id를 가져와서 그 Survey 의 상세분석을 가져옴
        Optional<SurveyAnalyze> surveyAnalyze = surveyAnalyzeRepository.findBySurveyDocumentId(surveyDocumentId);

        return surveyAnalyze.map(this::getSurveyDetailAnalyzeDto).orElse(null);
    }

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
            log.error("Error output:");
            while (true) {
                try {
                    if ((errorLine = errorReader.readLine()) == null) break;
                } catch (IOException e) {
                    throw new InvalidPythonException(e);
                }
                log.error(errorLine);
            }
        }

        log.error("Process exited with code " + exitCode);
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
