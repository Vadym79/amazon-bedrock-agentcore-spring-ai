package dev.vkazulkin.embabel.agent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;

import dev.vkazulkin.embabel.config.ConferenceConfig;
import dev.vkazulkin.embabel.domain.Domain;

@Agent(description = "create the talk, search for the conferences, and apply for them with the create talk")
public class CreateTalkAndApplyForConferencesAgent extends AbstractConferenceAgent {
	
	private final static Logger logger = LoggerFactory.getLogger(CreateTalkAndApplyForConferencesAgent.class);

	public CreateTalkAndApplyForConferencesAgent(ConferenceConfig config, ToolGroup toolGroup) {
		super(config, toolGroup);
	}
	
	@Action
	Domain.TalkCreationRequest extractTalkCreationRequest(UserInput userInput, OperationContext context) {
		logger.info("invoked talkCreationRequest with the request: " + userInput);
		return context.ai()
				//.withDefaultLlm()
				.withLlm(LlmOptions.withModel("us.amazon.nova-pro-v1:0"))
				.createObject("""
				Create a talk creation request from this user input, extracting the criteria like title and description conditions. 
				Don't include any other information.:
				%s""".formatted(userInput.getContent()), Domain.TalkCreationRequest.class);
	}
	
	@Action
	Domain.Talks createTalk(Domain.TalkCreationRequest talkCreationRequest, Ai ai) {
		logger.info("invoked createTalk with the request: "+talkCreationRequest);
		return config.speaker()		
			.promptRunner(ai)
			.withPromptContributors(List.of(talkCreationRequest))
			.withToolGroup(this.toolGroup)
			.createObject("Create the talk with the given criteria", Domain.Talks.class);
	}
	
}