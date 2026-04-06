#!/bin/sh
set -e
mvn clean package && cdk deploy -c awsAccountId={YOUR_AWS_ACCOUINT_ID} -c restApiId={YOUR_AMAZON_GATEWAY_REST_API_ID} --all