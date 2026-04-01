package dev.vkazulkin.conference;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.ToolParam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public record Conferences(Set<Conference> conferences) {

	public static void main(String[] args) throws Exception {

		var objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		try (var inputStream = TypeReference.class.getResourceAsStream("/conferences.json")) {
			 var conferences =objectMapper.readValue(inputStream, Conferences.class);
			 search("Serverless", conferences);
		}
	}
	
	
	public static Set<Conference> search(String topic, Conferences conferences) {
		System.out.println("search topic " + topic);
		Set<Conference> foundConferences = conferences.conferences().stream().filter(c -> c.topics().contains(topic))
				.collect(Collectors.toSet());

		System.out.println("return list of conferences: " + foundConferences);
		return foundConferences;
	}
}
