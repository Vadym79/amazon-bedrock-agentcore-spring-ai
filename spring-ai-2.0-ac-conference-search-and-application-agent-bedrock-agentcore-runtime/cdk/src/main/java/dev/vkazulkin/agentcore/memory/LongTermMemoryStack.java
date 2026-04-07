package dev.vkazulkin.agentcore.memory;

import java.util.List;

import dev.vkazulkin.ConventionalDefaults;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.Memory;
import software.amazon.awscdk.services.bedrock.agentcore.alpha.MemoryStrategy;
import software.constructs.Construct;

public class LongTermMemoryStack extends Stack {


    public LongTermMemoryStack(Construct scope, String appName,  StackProps stackProps) {
    	var id=ConventionalDefaults.stackName(appName, "lt-memory");
        super(scope, id, stackProps);   
        System.out.println(" stack id "+id);
        
        var memory = Memory.Builder.create(this, "long-term-memory-1")
              .memoryName("long_term_memory_for_conference_application")
              .description("Long-Term Memory for Conference Application")
        	  .expirationDuration(Duration.days(7))
        	  .memoryStrategies(List.of(MemoryStrategy.usingBuiltInSummarization(), MemoryStrategy.usingBuiltInSemantic()))
        	  .build();   
               
        CfnOutput.Builder.create(this, "LongTermMemoryIdOutput").value(memory.getMemoryId()).build();           
     }  
}