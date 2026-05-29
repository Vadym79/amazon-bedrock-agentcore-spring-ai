package dev.vkazulkin;

import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringAIConferenceAppOnAgentCoreRuntimeApplication {

	public static void main(String[] args) {
			SpringApplication.run(SpringAIConferenceAppOnAgentCoreRuntimeApplication.class, args);
	}

	@Bean ToolCallingManager toolCallingManager() {
		return ToolCallingManager.builder().build();
	}
}

