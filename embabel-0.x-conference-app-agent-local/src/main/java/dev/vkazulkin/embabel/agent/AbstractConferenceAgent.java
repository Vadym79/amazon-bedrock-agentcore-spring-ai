package dev.vkazulkin.embabel.agent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;

import dev.vkazulkin.embabel.config.ConferenceConfig;
import dev.vkazulkin.embabel.domain.Domain;
import dev.vkazulkin.embabel.tool.DateTimeTools;
import dev.vkazulkin.embabel.service.McpToolService;

abstract class AbstractConferenceAgent {

	private static final Logger logger = LoggerFactory.getLogger(AbstractConferenceAgent.class);

	protected final ConferenceConfig config;
	protected ToolGroup toolGroup;
	
	@Autowired
	protected DateTimeTools dateTimeTools;
    
	// handle JWT auth token expiration and mcp client connection timeout by invoking
	// this.toolGroup = mcpToolService.getToolGroup(); and using it with prompt runner
	protected final McpToolService mcpToolService; 	
    
	public AbstractConferenceAgent(ConferenceConfig config,ToolGroup toolGroup, McpToolService mcpToolService) {
		this.config = config;
		this.mcpToolService=mcpToolService;
		this.toolGroup = toolGroup;
	}

	@Action
	Domain.ConferenceSearchRequest extractConferenceSearchRequest(UserInput userInput, OperationContext context) {
		logger.info("invoked conferenceSearchRequest with the request: " + userInput);
		return context.ai()
				//.withDefaultLlm()
				.withLlm(LlmOptions.withModel("us.anthropic.claude-sonnet-4-6"))
				.createObject("""
				Create a conference search request from this user input, extracting optional information like conference topic, start and end dates and call for papers still open on the request date.
				Don't include any other not conference search related information like conference application and talk search details into this request.:
				%s""".formatted(userInput.getContent()), Domain.ConferenceSearchRequest.class);
	}

	@Action
	//@AchievesGoal(description = "provide the list of the conferences that match the search criteria")
	Domain.Conferences conferenceSearch(Domain.ConferenceSearchRequest conferenceSearchRequest, Ai ai) {
		logger.info("invoked conferenceSearch with the request: " + conferenceSearchRequest);
		return config.attendee().promptRunner(ai)
				.withPromptContributors(List.of(conferenceSearchRequest))
				.withToolGroup(this.toolGroup)
				.withToolObject(dateTimeTools)
				.createObject("""
						Search for the conference with the given criteria. 
						If you need to get the current time, use DateTimeTools tool for it.
						""", Domain.Conferences.class);
						
	}
	
	@Action
	@AchievesGoal(description = "apply for the conferences with their IDs and talk IDs and provide status of the application")
	Domain.ConferenceApplications applyForConference(Domain.Conferences conferences, Domain.Talks talks, Ai ai) {
		logger.info("invoked applyForConference with the request: conferences: " + conferences+ " talks: "+talks);
		return config.speaker()
				.promptRunner(ai)
				.withPromptContributors(List.of(conferences, talks))
				.withToolGroup(this.toolGroup)
				.createObject("Apply for the conference with the given criteria", Domain.ConferenceApplications.class);
	}
}