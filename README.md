Java Spring sandbox backend app - (that i think) easy to scale.

Some demonstrated feature:

- AWS Aurora (PostgreSQL) for dynamic parameter registry
- AWS ElasticSearch (Redis) for authentication management
- AWS CloudWatch for log/monitoring
- Symmetric encryption APIs
- TBD.

## Build command

Environment variables on `/resources/application.yml`

```bash
./mvnw clean package
```

or use existing `mvn` if you already have

## Disclaimer

Since this is a continuous sandbox project, the negative case validation here and there are still lacking. But it's
swagger-ready to test 🌲