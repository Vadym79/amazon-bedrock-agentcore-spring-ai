package dev.vkazulkin.conference;

import java.time.LocalDate;
import java.util.Set;

public record Conference (Integer id, String name, Set<String> topics, String homepage, 
		LocalDate startDate, LocalDate endDate, LocalDate callForPaperStartDate, LocalDate callForPaperEndDate, 
		String city, String linkToCallforPapers) {
}


