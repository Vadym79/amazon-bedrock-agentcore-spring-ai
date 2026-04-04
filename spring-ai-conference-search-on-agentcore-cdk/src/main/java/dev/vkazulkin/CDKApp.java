package dev.vkazulkin;

import dev.vkazulkin.agentcore.gateway.AgentCoreGatewayWithMCPTargetStack;
import dev.vkazulkin.agentcore.runtime.AgentCoreRuntimeWithMCPStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public interface CDKApp {
    
    String appName = "spring-ai-conference-search-agentcore";

    static void main(String... args) {

        var app = new App();
        new AgentCoreRuntimeWithMCPStack(app, appName, stackProperties());
        new AgentCoreGatewayWithMCPTargetStack(app, appName, stackProperties());
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

