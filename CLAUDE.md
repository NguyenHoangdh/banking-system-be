# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Run the application
./mvnw spring-boot:run

# Build JAR
./mvnw clean package
java -jar target/banking-0.0.1-SNAPSHOT.jar

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=BankingApplicationTests

# Full build with tests
./mvnw clean verify
```

**Prerequisites:** MySQL running on `localhost:3306` with a `bank` database created. Java 21 required.

Swagger UI is available at `http://localhost:8080/swagger-ui.html` when running.

## Architecture

Layered Spring Boot REST API following the pattern: **Controller → Service Interface → ServiceImpl → Repository → Entity**

```
com.nghoang.banking/
├── BankingApplication.java      # Entry point; configures OpenAPI/Swagger
├── config/
│   └── SecurityConfig.java      # BCrypt, stateless JWT sessions, CSRF disabled
├── controller/
│   ├── UserController.java      # Account management endpoints
│   └── TransactionController.java
├── service/
│   ├── UserService / UserServiceImpl       # Core business logic: create account, balance enquiry, credit/debit/transfer
│   ├── EmailService / EmailServiceImpl     # Gmail SMTP notifications
│   ├── TransactionService / TransactionImpl
│   ├── BankStatement.java                  # PDF statement generation (iText)
│   ├── CustomUserDetailService.java        # Spring Security user loading
│   └── JwtTokenProvider.java              # JWT creation & validation
├── repository/
│   ├── UserRepository.java
│   └── TransactionRepository.java
├── entity/
│   ├── User.java          # Account holder; accountNumber is auto-generated (not DB PK)
│   └── Transaction.java
├── dto/                   # Request/response DTOs; BankResponse is the standard API envelope
└── utils/
    └── AccountUtils.java  # Account number generation, response codes/messages
```

**Key public endpoint:** `POST /api/user` (account creation) — all others require JWT authentication.

**Note:** `JwtTokenProvider` and the JWT filter infrastructure exist but `doFilterInternal` is currently empty — authentication enforcement is incomplete.

## Key Dependencies

- **Spring Boot 4.0.5**, Java 21, Maven
- **MySQL** via Spring Data JPA (Hibernate, `spring.jpa.hibernate.ddl-auto=update`)
- **JJWT 0.12.3** for JWT tokens
- **SpringDoc OpenAPI 2.1.0** for Swagger UI
- **iText PDF 5.5.13.5** for bank statement generation
- **Lombok** — used heavily (`@Data`, `@Builder`, `@RequiredArgsConstructor`, `@AllArgsConstructor`)

## Configuration

`src/main/resources/application.properties` contains database credentials, Gmail SMTP settings, and the JWT secret/expiration. These are currently hardcoded and should be externalized via environment variables for any non-local deployment.
