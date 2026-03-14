package com.urviclean.recordbook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RecordbookApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(RecordbookApplication.class);
		application.run(withExternalConfigIfProvided(args));
	}

	private static String[] withExternalConfigIfProvided(String[] args) {
		List<String> normalizedArgs = new ArrayList<>(Arrays.asList(args));
		for (String arg : args) {
			if (arg != null && arg.startsWith("--config.file=")) {
				String configPath = arg.substring("--config.file=".length()).trim();
				if (!configPath.isEmpty()) {
					normalizedArgs.add("--spring.config.location=file:" + configPath);
				}
			}
		}
		return normalizedArgs.toArray(new String[0]);
	}
}


