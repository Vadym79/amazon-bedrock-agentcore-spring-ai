package dev.vkazulkin.embabel.agent.domain;

import java.time.LocalDate;
import java.util.Set;

import com.embabel.common.ai.prompt.PromptContributor;

public class Domain {
	
		
	public record Conference (Integer conferenceId, String name, Set<String> topics, String homepage, 
			LocalDate startDate, LocalDate endDate, LocalDate callForPapersStartDate, LocalDate callForPapersEndDate, 
			String city, String linkToCallforPapers) {
	}

	public record Conferences(Set<Conference> conferences) implements PromptContributor  {

		@Override
		public String contribution() {
			return "- %s:".formatted(conferences);
		}
	}
	
	public record Talks(Set<Talk> conferenceTalks) implements PromptContributor  {

		@Override
		public String contribution() {
			return "- %s:".formatted(conferenceTalks);
		}
	}
	
	public record Talk(int talkId, String title, String description) {}
	
	public record TalkSearchRequest(String titleSubString) implements PromptContributor  {

		@Override
		public String contribution() {
			return "- %s ".formatted(titleSubString);
		}		
	}

	public record TalkCreationRequest(String title, String description) implements PromptContributor  {

		@Override
		public String contribution() {
			return "-  %s: %s ".formatted(title, description);
		}		
	}
	
	public record ConferenceSearchRequest(String topic, LocalDate startDate, 
			LocalDate endDate, LocalDate callForPapersOpenOnThistDate) implements PromptContributor  {

		@Override
		public String contribution() {
			return "- %s: %s: %s: %s".formatted(topic, startDate, endDate, callForPapersOpenOnThistDate);
		}		
	}
	
	public record ConferenceApplicationRequest(int conferenceId, int talkId) implements PromptContributor  {

		@Override
		public String contribution() {
			return "- %s: %s".formatted(conferenceId, talkId);
		}
	}
		
	public record ConferenceApplication(int conferenceId, int talkId, String status) {}
	
	public record ConferenceApplications(Set<ConferenceApplication> conferenceApplications) {}

}
