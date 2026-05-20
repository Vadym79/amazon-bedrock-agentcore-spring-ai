package dev.vkazulkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import tools.jackson.databind.ObjectMapper;

@SpringBootApplication
public class SpringAIConferenceAppOnAgentCoreRuntimeApplication {

	public static void main(String[] args) {
			SpringApplication.run(SpringAIConferenceAppOnAgentCoreRuntimeApplication.class, args);
	}

	@Bean
    public ObjectMapper objectMapper() {
       return new ObjectMapper();
    }
}

