package dev.vkazulkin.conference;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public record Conferences (Set<Conference> conferences) {

	public static void main(String[] args) throws JsonProcessingException {
		
		var conf1= new Conference(1,"AWS Re:Invent", Set.of("AWS Cloud", "Serverless", "Gen AI"),
				"https://reinvent.awsevents.com", LocalDate.of(2025, Month.DECEMBER, 1),
				LocalDate.of(2025, Month.DECEMBER, 5), LocalDate.of(2025, Month.JULY, 1),
				LocalDate.of(2025, Month.AUGUST,31), "Las Vegas", "https://reinvent.awsevents.com/cfp");
		
		var conf2= new Conference(2,"Voxxed Days Belgium", Set.of("Java", "JVM", "Spring", "AI"),
				"https://devoxx.be/", LocalDate.of(2025, Month.OCTOBER, 6),
				LocalDate.of(2025, Month.OCTOBER, 10),  LocalDate.of(2025, Month.MAY, 1),
				LocalDate.of(2025, Month.JUNE,30), "Antwerpen", "https://devoxx.be/cfp");
		
		var conf3= new Conference(3,"PyCon", Set.of("Python", "ML", "AI"),
				"https://pycon.com/", LocalDate.of(2025, Month.NOVEMBER, 4),
				LocalDate.of(2025, Month.NOVEMBER, 6),  LocalDate.of(2025, Month.JULY, 1),
				LocalDate.of(2025, Month.AUGUST,31), "Sydney", "https://pycon/cfp");
		
		var conf4= new Conference(4,"Serverless Days Sao Paolo", Set.of("AWS Cloud", "Azure Cloud", "Google Cloud", 
				"Serverless", "Gen AI"),"https://saopaolo-serverlessdays.io", LocalDate.of(2025, Month.NOVEMBER, 8),
				LocalDate.of(2025, Month.NOVEMBER, 8),  LocalDate.of(2025, Month.JULY, 1),
				LocalDate.of(2025, Month.AUGUST,31), "Sao Paolo", "https://saopaolo-serverlessdays.io");
		
		var conf5= new Conference(5,"JCON Europe", Set.of("Java", "JVM", "Spring", "AI", "Cloud"),
				"https://2026.europe.jcon.one/", LocalDate.of(2026, Month.APRIL, 20),
				LocalDate.of(2026, Month.APRIL, 23),  LocalDate.of(2025, Month.DECEMBER, 1),
				LocalDate.of(2026, Month.JANUARY,31), "Cologne", "https://2026.europe.jcon.one/cfp");
		
		
		var conferences= new Conferences(Set.of(conf1, conf2, conf3, conf4, conf5));
		
		var objectMapper= new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		
		System.out.print(objectMapper.writeValueAsString(conferences));
	
	}

}
