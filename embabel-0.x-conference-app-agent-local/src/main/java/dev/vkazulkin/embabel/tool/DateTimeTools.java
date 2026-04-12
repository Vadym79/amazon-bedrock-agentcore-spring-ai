package dev.vkazulkin.embabel.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.embabel.agent.api.annotation.LlmTool;
import java.time.LocalDate;

@Component
public class DateTimeTools {
	
	private static final Logger logger = LoggerFactory.getLogger(DateTimeTools.class);

    @LlmTool(description = "Get the current date")
    //Tool(description = "Get the current date ")
    String getLocalDate() {
        var localDate= LocalDate.now().toString();
        logger.info("invoke tool to return the local date which is "+localDate);
        return localDate;
    }

}