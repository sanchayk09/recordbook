package com.urviclean.recordbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RecordbookApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecordbookApplication.class, args);
	}

}


