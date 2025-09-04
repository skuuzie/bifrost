Java Spring practice/sandbox backend app - (that i think) proper and easy to modify

Currently utilizing AWS products for deployment, database, and logging/monitoring. Namely AWS CloudWatch, Aurora DSQL, API Gateway, and EC2.

Some demonstrated feature:

- Dynamic runtime system parameter registry (i.e. modify variable without redeployment)
- OTP & JWT for authentication
- Remote logging/monitoring
- Symmetric encryption APIs
- AI (Google Gemini) Resume Roaster with Async Processing & Caching
- Simple controller-level unit testing
- TBD.

Stack:

- Java Spring
- PostgreSQL
- Redis

## Build command

Environment variables on `/resources/application.yml`

```bash
./mvnw clean package
```

or use existing `mvn` if you already have

## Disclaimer

Since this is a continuous sandbox project, the negative case validation here and there are still lacking. But it's
swagger-ready to test 🌲