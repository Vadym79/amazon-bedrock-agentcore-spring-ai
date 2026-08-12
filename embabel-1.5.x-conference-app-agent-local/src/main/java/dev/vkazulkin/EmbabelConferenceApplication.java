package dev.vkazulkin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;

import com.embabel.agent.config.models.bedrock.BedrockModelLoader;

import dev.vkazulkin.embabel.config.ConferenceConfig;
import dev.vkazulkin.embabel.service.McpToolService;
import io.modelcontextprotocol.client.McpSyncClient;
import tools.jackson.databind.json.JsonMapper;



@SpringBootApplication
@EnableConfigurationProperties(ConferenceConfig.class)
public class EmbabelConferenceApplication {

	@Autowired
    private McpToolService mcpToolService;
	
	public static void main(String[] args) {
			SpringApplication.run(EmbabelConferenceApplication.class, args);
	}
	
	
	@Bean
    public JsonMapper objectMapper() {
		JsonMapper mapper = new JsonMapper();
        // Custom configurations go here
        return mapper;
    }
	
	@Bean 
	@Primary
	public McpSyncClient geMcpClient() {
		return this.mcpToolService.getMcpClient();
	}
	
	@Configuration
	class AdditionalBedrockModels {

		@Bean
		BedrockModelLoader bedrockModels() {
			return new BedrockModelLoader(new DefaultResourceLoader(), "classpath:models/additional-bedrock.yaml");
		}

	}	
}