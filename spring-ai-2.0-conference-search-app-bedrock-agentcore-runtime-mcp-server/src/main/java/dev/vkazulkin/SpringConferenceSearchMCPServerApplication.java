package dev.vkazulkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import tools.jackson.databind.ObjectMapper;

@SpringBootApplication
public class SpringConferenceSearchMCPServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringConferenceSearchMCPServerApplication.class, args);
	}

	/*
	@Bean
	public List<ToolCallback> conferenceSearchTools(ConferenceSearchTools conferenceSearchTool) {
		return List.of(ToolCallbacks.from(conferenceSearchTool));
	}
	*/
	
	@Bean
    public ObjectMapper objectMapper() {
       return new ObjectMapper();
    }
}
