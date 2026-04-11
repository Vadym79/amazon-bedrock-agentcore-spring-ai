package dev.vkazulkin.embabel.agent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

abstract class AbstractConferenceAgent {

	private final static Logger logger = LoggerFactory.getLogger(AbstractConferenceAgent.class);

	protected ConferenceConfig config;
	protected ToolGroup toolGroup;

	public AbstractConferenceAgent(ConferenceConfig config, ToolGroup toolGroup) {
		this.config = config;
		this.toolGroup = toolGroup;
	}

	@Action
	Domain.ConferenceSearchRequest extractConferenceSearchRequest(UserInput userInput, OperationContext context) {
		logger.info("invoked conferenceSearchRequest with the request: " + userInput);
		return context.ai()
				//.withDefaultLlm()
				.withLlm(LlmOptions.withModel("us.amazon.nova-pro-v1:0"))
				.createObject("""
				Create a conference search request from this user input, extracting optional information like conference topic, start and end dates and call for papers still open on the request date.
				Don't include any other information.:
				%s""".formatted(userInput.getContent()), Domain.ConferenceSearchRequest.class);
	}

	@Action
	Domain.Conferences conferenceSearch(Domain.ConferenceSearchRequest conferenceSearchRequest, Ai ai) {
		logger.info("invoked conferenceSearch with the request: " + conferenceSearchRequest);
		return config.attendee().promptRunner(ai)
				.withPromptContributors(List.of(conferenceSearchRequest))
				.withToolGroup(this.toolGroup)
				.withToolObject(new DateTimeTools())
				.createObject("search for the conference with the given criteria", Domain.Conferences.class);
	}

	@Action
	@AchievesGoal(description = "apply for the conferences with their IDs and talk IDs and provide status of the application")
	Domain.ConferenceApplications applyForConference(Domain.Conferences conferences, Domain.Talks talks, Ai ai) {
		logger.info("invoked applyForConference with the request: conferences: " + conferences+ " talks: "+talks);
		return config.speaker()
				.promptRunner(ai)
				.withPromptContributors(List.of(conferences, talks))
				.withToolGroup(this.toolGroup)
				.createObject("apply for the conference with the given criteria", Domain.ConferenceApplications.class);
	}
}