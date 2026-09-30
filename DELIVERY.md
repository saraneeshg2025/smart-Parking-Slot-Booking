# ParkSmart Delivery Guide

## Project status
ParkSmart is ready for delivery. The application builds and runs successfully, and the automated test suite passes.

## Verified commands
- Test suite: `./mvnw test -q`
- Production package: `./mvnw clean package -q`
- Run locally: `./mvnw spring-boot:run`

## Runtime requirements
- Java 17+
- MySQL server running locally
- Environment variable set before startup:

```bash
set MYSQL_PASSWORD=your_mysql_password
```

On Linux/macOS use:

```bash
export MYSQL_PASSWORD=your_mysql_password
```

## App configuration
The app expects MySQL at:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/grain_storage_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=${MYSQL_PASSWORD}
```

## Local startup
From the project root:

```bash
./mvnw spring-boot:run
```

Then open the app at:

```text
http://localhost:8082/
```

## Notes
- The project uses Spring Boot 3.3.4
- Test execution uses an H2 in-memory profile and is already configured
- Secrets are not hardcoded in source; the MySQL password is read from `MYSQL_PASSWORD`
- The app includes validation, booking logic, penalties, parking lot operations, and controller/API endpoints

## Suggested delivery handoff
1. Ensure MySQL is installed and running.
2. Set the `MYSQL_PASSWORD` environment variable in the target environment.
3. Run the app using `./mvnw spring-boot:run`.
4. Validate the endpoints and admin operations in a browser or API client.
