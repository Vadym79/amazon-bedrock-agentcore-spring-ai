package dev.vkazulkin.conference;

import java.time.LocalDate;
import java.util.Set;

public record Conference (Integer conferenceId, String name, Set<String> topics, String homepage, 
		LocalDate startDate, LocalDate endDate, LocalDate callForPapersStartDate, LocalDate callForPapersEndDate, 
		String city, String linkToCallforPapers) {
}


