package dev.vkazulkin.agentcore.memory;

import dev.vkazulkin.ConventionalDefaults;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.Memory;
import software.constructs.Construct;

public class ShortTermMemoryStack extends Stack {


    public ShortTermMemoryStack(Construct scope, String appName,  StackProps stackProps) {
    	var id=ConventionalDefaults.stackName(appName, "st-memory");
        super(scope, id, stackProps);   
        System.out.println(" stack id "+id);
        var memory = Memory.Builder.create(this, "short-term-memory-1")
              .memoryName("short-term-memory-for-conference-application")
              .description("Short-Term Memory for Conference Application")
        	  .expirationDuration(Duration.days(7))
        	  .build();   
               
        CfnOutput.Builder.create(this, "ShortTermMemoryIdOutput").value(memory.getMemoryId()).build();           
     }  
}