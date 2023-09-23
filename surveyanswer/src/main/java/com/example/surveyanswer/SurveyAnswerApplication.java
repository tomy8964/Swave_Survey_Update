package com.example.surveyanswer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SurveyAnswerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SurveyAnswerApplication.class, args);
	}

}
