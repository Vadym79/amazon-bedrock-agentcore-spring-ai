package dev.vkazulkin.agent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;

import dev.vkazulkin.embabel.agent.Domain;


@RestController
public class EmbabelAgentController {

	private final AgentPlatform agentPlatform;
	private final AgentInvocation<Domain.ConferenceApplications> invocation;
	
	private final static Logger logger = LoggerFactory.getLogger(EmbabelAgentController.class);


	public EmbabelAgentController(AgentPlatform agentPlatform) {
		this.agentPlatform = agentPlatform;
		this.invocation = AgentInvocation.builder(agentPlatform).build(Domain.ConferenceApplications.class);
	}
	
	/**
	 * GET method which has a prompt as an input parameter and outputs the agent response synchronously
	 * 
	 * @param prompt - prompt
	 * @return agent answer
	 */
	@GetMapping(value = "/conference", consumes = "text/plain")
	public Domain.ConferenceApplications conferenceApplications(@RequestParam String prompt) {
		logger.info("invocations endpoint with prompt: " + prompt);

		return invocation.invoke(null);
	}

}
