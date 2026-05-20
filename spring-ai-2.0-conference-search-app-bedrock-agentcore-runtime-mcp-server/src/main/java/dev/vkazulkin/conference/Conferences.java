package dev.vkazulkin.conference;

import java.util.Set;


import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

public record Conferences(Set<Conference> conferences) {

	public static void main(String[] args) throws Exception {

		var objectMapper = new ObjectMapper();
	
		try (var inputStream = TypeReference.class.getResourceAsStream("/conferences.json")) {
			 var conferences =objectMapper.readValue(inputStream, Conferences.class);
			 search("Serverless", conferences);
		}
	}
	
	
	private static void search(String topic, Conferences conferences) {
		System.out.println("search topic " + topic);
		var foundConferences = conferences.conferences()
				.stream().filter(c -> c.topics().contains(topic))
				.toList();

		System.out.println("return list of conferences: " + foundConferences);
		System.out.println("return call for papers end date of the first conference: " + foundConferences.getFirst().callForPapersEndDate());
	}
}
