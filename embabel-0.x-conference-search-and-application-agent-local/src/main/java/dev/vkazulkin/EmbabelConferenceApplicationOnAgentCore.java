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
import com.embabel.agent.core.ToolGroup;

import dev.vkazulkin.embabel.config.ConferenceConfig;
import dev.vkazulkin.embabel.service.McpToolService;



@SpringBootApplication
@EnableConfigurationProperties(ConferenceConfig.class)
public class EmbabelConferenceApplicationOnAgentCore {

	@Autowired
    private McpToolService mcpToolService;
	
	public static void main(String[] args) {
			SpringApplication.run(EmbabelConferenceApplicationOnAgentCore.class, args);
	}
	
	@Bean 
	@Primary
	public ToolGroup getToolGroup() {
		return this.mcpToolService.getToolGroup();
	}
	
	
	@Configuration
	class AdditionalBedrockModels {

		@Bean
		BedrockModelLoader bedrockModels() {
			return new BedrockModelLoader(new DefaultResourceLoader(), "classpath:models/additional-bedrock.yaml");
		}

	}
	
	
}
