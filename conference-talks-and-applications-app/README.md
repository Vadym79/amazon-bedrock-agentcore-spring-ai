## Compile and package the Java application with Maven from the root (where pom.xml is located) of the project

```bash
mvn clean package
```

## Deploy your application with AWS SAM  

```bash
sam deploy -g --region us-east-1 
```

## Sample request body to create conference applications:  

 [ 
	{
	"conferenceId" : 1,
	"talkId" : 1
	}, {
	"conferenceId" : 2,
	"talkId" : 1
	},
	{
	"conferenceId" : 2,
	"talkId" : 2
	} 
 ] 

