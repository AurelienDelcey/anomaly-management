# Anomaly Management

A desktop application for managing anomalies in an industrial environment, from creation to final resolution.

Traceability and persistence are fully implemented.

This project is inspired by my current work experience in an automotive forging industry.

---

## Overview

This application is designed for industrial workers such as operators, team leaders, quality employees, and supervisors.

Its purpose is to manage anomalies in a clear and structured way, while allowing unresolved issues to be extended until a satisfactory resolution is reached.

Each anomaly follows a strict lifecycle:

`PENDING → CORRECTED → RESOLVED → ARCHIVED`

Every transition is controlled by business rules and constraints.

If a resolution is rejected during the archiving step, a linked prolongation anomaly is automatically created to continue the process.

This project was built to demonstrate layered architecture principles and the modeling of strong business rules.

---

## Features

- Create and manage anomalies through their full lifecycle
- Browse anomalies with pagination, sorting, and filtering
- Open detailed anomaly views
- View lifecycle traceability and history
- Apply controlled state transitions based on business rules
- Role-based permissions depending on the current actor
- Automatic creation of prolongation anomalies when a resolution is rejected
- Navigation through prolongation history with lazy loading
- Full prolongation history view cached while the detail view remains open
- Persistence using MySQL and JDBC
- Atomic transaction handling during prolongation creation
- Fast-fail detection of database corruption or structural inconsistencies
- Desktop user interface built with JavaFX

---

## Tech Stack

- Java 21
- JavaFX
- MySQL 8
- JDBC
- Maven
- JUnit 5
- Docker / Docker Compose
- Bash
- SLF4J
- Logback

---

## Architecture

This project is structured around the business core.

The application is organized into isolated layers with clear responsibilities:

- Domain: business rules, invariants, lifecycle logic, immutable business entities and value objects
- Application: use cases, orchestration, typed results, DTO projections
- Infrastructure: persistence layer and technical implementations
- User Interface: JavaFX presentation layer

Additional design choices:

- Persistence mechanisms can be replaced with minimal changes
- Read and write flows are separated
- Read models are exposed through DTOs
- Typed error handling is preferred over generic failures
- Business logic remains independent from UI and database concerns

```text
src/main/java
├── domain
├── application
├── infrastructure
└── userInterface
```

---

## Business Rules

- An anomaly cannot be created with invalid data
- State transitions are strictly controlled by lifecycle rules
- No backward transition is allowed once a state has been reached
- An anomaly follows the lifecycle: `PENDING → CORRECTED → RESOLVED → ARCHIVED`
- Archived anomalies are immutable
- A corrected anomaly requires a corrective action
- A corrected anomaly also requires a quality decision
- A resolved anomaly requires evidence proving the effectiveness of the corrective action
- Rejected resolutions automatically generate a linked prolongation anomaly
- Creation data can only be edited while the anomaly is in `PENDING` state and is not a prolongation
- A prolongation anomaly inherits the creation context of the initial anomaly
- Linked anomalies keep parent / child traceability references
- Invalid operations are rejected explicitly

---

## Getting Started

### Requirements

- Java 21
- Maven
- Docker
- Git

### Clone the repository

```bash
git clone https://github.com/AurelienDelcey/anomaly-management.git
cd anomaly-management
```

### Linux (recommended)

```bash
chmod +x run.sh
./run.sh
```

### Windows

```bash
docker compose up -d
java -jar target/anomaly-management-1.0.0.jar --name=John --role=supervisor --id=1234
```

During the first launch, database initialization may take approximately one minute.

---

## Usage

```bash
./run.sh --name John --role supervisor --id 1234
```

If one argument is missing or invalid, the application starts with the default actor.

Available roles:

- read_only
- operator
- supervisor

---

## Tests

The project includes more than 200 automated tests covering the main application behaviors.

Covered areas include:

- Domain invariants
- Lifecycle transitions
- Value objects validation
- Application services
- Query flows
- Business ID generation
- JDBC persistence behavior

Integration tests run against a real MySQL database using dedicated test tables.

```bash
mvn test
```

---

## Design Choices

- Business entities are immutable by default
- The domain layer protects internal consistency and business rules
- No business entity leaves the service layer directly
- Read models are exposed through DTO projections
- Service APIs return typed results
- Commands expose explicit success / failure outcomes with dedicated payloads
- Queries also use typed result contracts
- UUIDs are used internally to identify anomalies
- Human-readable Business IDs are generated separately
- Business ID generation includes retry logic in case of collision
- Application services are independent from the UI layer
- User-facing messages are clear and understandable
- Logs are oriented toward technical diagnosis
- Configuration is externalized through environment variables
- Read and write responsibilities are intentionally separated

---

## Future Improvements

- Improve UX during transitions with smarter validation
- Real file management with system default opening
- Logical constraints between sectors and machines
- Additional anomaly categories
- Quarantine tables for corrupted data requiring manual recovery
- Multi-user distributed architecture planned for a future V2

---

## Author

Created by **Aurélien Delcey**

Personal software engineering project focused on business modeling, layered architecture, and robust application design.

- GitHub: https://github.com/AurelienDelcey
- LinkedIn: https://www.linkedin.com/in/aurelien-delcey

---

## Status

**Version 1.0.0**

This project currently provides a complete single-user desktop anomaly management workflow with persistence, traceability, testing, and automated startup tooling.

Future versions may explore multi-user and distributed architecture.
