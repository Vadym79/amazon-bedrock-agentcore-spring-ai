package dev.vkazulkin.embabel.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.embabel.agent.core.Agent;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.agent.core.Verbosity;
import com.embabel.agent.domain.io.UserInput;

import dev.vkazulkin.embabel.agent.CreateTalkAndApplyForConferencesAgent;
import dev.vkazulkin.embabel.agent.SearchForTalksAndApplyForConferencesAgent;
import dev.vkazulkin.embabel.domain.Domain;


@RestController
public class EmbabelAgentController {

	private final AgentPlatform agentPlatform;
	// private final AgentInvocation<Domain.ConferenceApplications> invocation;
	
	private static final Logger logger = LoggerFactory.getLogger(EmbabelAgentController.class);
	private ProcessOptions processOptions = new ProcessOptions()
		    .withVerbosity(new Verbosity()
		    .withShowPrompts(true)
		    .withShowLlmResponses(true)
		    .withDebug(true));


	public EmbabelAgentController(AgentPlatform agentPlatform) {
		this.agentPlatform = agentPlatform;
		
		/*
		this.invocation = AgentInvocation
				.builder(agentPlatform)
				.options(processOptions)
				.build(Domain.ConferenceApplications.class);
		*/
	}
	
	/**
	 * GET method which has a prompt as an input parameter and outputs the agent response synchronously
	 * 
	 * @param prompt - prompt
	 * @return agent answer
	 */
	@GetMapping(value = "/applyToConferencesWithExistingTalks", consumes = "text/plain")
	public Domain.ConferenceApplications applyToConferencesWithExistingTalks(@RequestParam String prompt) {
		logger.info("applyToConferencesWithExistingTalks invoked with prompt: " + prompt);        
	    return this.invokeAgent(prompt, SearchForTalksAndApplyForConferencesAgent.AGENT_NAME);	
	}
	

	/**
	 * GET method which has a prompt as an input parameter and outputs the agent response synchronously
	 * 
	 * @param prompt - prompt
	 * @return agent answer
	 */
	@GetMapping(value = "/applyToConferencesWithNewTalks", consumes = "text/plain")
	public Domain.ConferenceApplications applyToConferencesWithNewTalks(@RequestParam String prompt) {
		logger.info("applyToConferencesWithNewTalks invoked with prompt: " + prompt);        
	    return this.invokeAgent(prompt, CreateTalkAndApplyForConferencesAgent.AGENT_NAME);	
	}

	
	private Domain.ConferenceApplications invokeAgent(String prompt, String agentName) {		
		logger.info("applyToConferenceWithExistingTalks invoked with prompt: " + prompt);        
		logger.info("agent platform agents " + this.agentPlatform.agents());
		
		var inputs = Map.of("request", new UserInput(prompt));
		
		var agent= this.getByName(agentName);
		var agentProcess=this.agentPlatform.createAgentProcess(agent, processOptions, inputs);
		var completedProcess = agentProcess.run();
		return completedProcess.last(Domain.ConferenceApplications.class);
	}
	
	private Agent getByName(String agentName) {
	   var optionalAgent= this.agentPlatform.agents().stream()
			   .filter(a -> a.getName().equals(agentName)).findFirst();
	   
	   if( optionalAgent.isEmpty()) {
			throw new RuntimeException("agent with the name "+agentName+ " not found");
		}
	   
	   var agent= optionalAgent.get();
	   logger.info("found agent "+agent);
	   return agent;
	}

}
