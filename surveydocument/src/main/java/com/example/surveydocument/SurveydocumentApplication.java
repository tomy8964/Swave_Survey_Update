package com.example.surveydocument;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SurveydocumentApplication {

	public static void main(String[] args) {
		SpringApplication.run(SurveydocumentApplication.class, args);
	}

}
