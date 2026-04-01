package dev.vkazulkin.conference;

import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public record Conferences(Set<Conference> conferences) {

	public static void main(String[] args) throws Exception {

		var objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		try (var inputStream = TypeReference.class.getResourceAsStream("/conferences.json")) {
			System.out.print(objectMapper.readValue(inputStream, Conferences.class));
		}
	}
}
