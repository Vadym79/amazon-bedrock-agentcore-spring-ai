package dev.vkazulkin;

import dev.vkazulkin.agentcore.memory.LongTermMemoryStack;
import dev.vkazulkin.agentcore.memory.ShortTermMemoryStack;
import dev.vkazulkin.agentcore.runtime.RuntimeWithMCPStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public interface CDKApp {
    
    String appName = "spring-ai-ac-conference-application";

    static void main(String... args) {

        var app = new App();
        new RuntimeWithMCPStack(app, appName, stackProperties());
        new ShortTermMemoryStack(app, appName, stackProperties());
        new LongTermMemoryStack(app, appName, stackProperties());
        app.synth();  
    }
    
    public static StackProps stackProperties() {
        var env = Environment
                .builder()
                .region("us-east-1")
                .build();
        return StackProps
                .builder()
                .env(env)
                .build();
    }
}

