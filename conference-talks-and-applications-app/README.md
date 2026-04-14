# Conference Talks and Applications App

A serverless Java application deployed on AWS Lambda via API Gateway that manages conference talks and speaker applications.

## What it does

Exposes three REST API endpoints (protected by API key):

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/talks` | Create a new conference talk (title + description) |
| `GET` | `/talks/{titleSubstring}` | Search talks by title substring |
| `POST` | `/apply` | Submit a conference application (conferenceId + talkId) |

## Architecture

- **Runtime**: Java 25 on AWS Lambda
- **Infrastructure**: AWS SAM (API Gateway + Lambda + CloudWatch Logs)
- **API auth**: API key via `x-api-key` header
- **Usage plan**: 100 requests/day, rate limit 100 req/s, burst 50

## Prerequisites

- Java 25
- Maven
- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html)
- AWS credentials configured

## Build

```bash
mvn clean package
```

This produces `target/conference-talks-and-applications-app-1.0.0-SNAPSHOT.jar`.

## Deploy

```bash
sam deploy -g --region us-east-1
```

After deployment, the API endpoint is printed as `ConferenceTalksAndApplicationsAppAPIEndpoint`.

## Sample requests

**Create a talk:**
```bash
curl -X POST https://<api-id>.execute-api.us-east-1.amazonaws.com/prod/talks \
  -H "x-api-key: a6ZbcDgjkQW10BN56ASR25" \
  -H "Content-Type: application/json" \
  -d '{"title": "My Talk", "description": "Talk description"}'
```

**Search talks by title:**
```bash
curl https://<api-id>.execute-api.us-east-1.amazonaws.com/prod/talks/Java \
  -H "x-api-key: a6ZbcDgjkQW10BN56ASR25"
```

**Submit a conference application:**
```bash
curl -X POST https://<api-id>.execute-api.us-east-1.amazonaws.com/prod/apply \
  -H "x-api-key: a6ZbcDgjkQW10BN56ASR25" \
  -H "Content-Type: application/json" \
  -d '{"conferenceId": 1, "talkId": 1}'
```
