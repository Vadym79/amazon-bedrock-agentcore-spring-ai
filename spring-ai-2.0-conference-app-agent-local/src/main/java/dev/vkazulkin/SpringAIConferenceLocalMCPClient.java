package dev.vkazulkin;

import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringAIConferenceLocalMCPClient {

	public static void main(String[] args) {
			SpringApplication.run(SpringAIConferenceLocalMCPClient.class, args);
	}
	
	@Bean ToolCallingManager toolCallingManager() {
		return ToolCallingManager.builder().build();
	}
}
