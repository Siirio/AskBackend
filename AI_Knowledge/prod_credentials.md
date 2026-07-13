# Production Database Access

**Host:** 212.19.134.80
**Port:** 5432
**Database:** ask
**User:** choki_ai_deployer
**Password:** 1234567890

Connection string: `jdbc:postgresql://212.19.134.80:5432/ask`

## Important
- NEVER delete the Docker container or database
- NEVER modify already-applied Flyway migrations
- Always create new migrations for schema changes
- Firewall may be IP-restricted — connect from authorized IP only
