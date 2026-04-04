package dev.vkazulkin;

public interface ConventionalDefaults {


    static String stackName(String appName,String stackName){
        return "%s-%s-stack".formatted(appName,stackName);
    }

    
    static String replaceAWSAccountID(String configParam, String awsAccountId) {
    	return configParam.replace("{AWS_ACCOUNT_ID}", awsAccountId);
    }
    
}
