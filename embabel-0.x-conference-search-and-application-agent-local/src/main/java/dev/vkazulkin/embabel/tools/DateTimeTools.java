package dev.vkazulkin.embabel.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DateTimeTools {
	
	private static final Logger logger = LoggerFactory.getLogger(DateTimeTools.class);

    @Tool(description = "Get the current date ")
    String getLocalDate() {
        var localDate= LocalDate.now().toString();
        logger.info("called tool to return the local date which is "+localDate);
        return localDate;
    }

}