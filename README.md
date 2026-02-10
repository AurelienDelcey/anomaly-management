# Anomaly Management

## 1. Context & Objective

- Build an application using a professional methodology.
- Design an application with a strong and strict business rules core.
- Use this project to practice several “advanced” coding concepts:
  - separation of concerns
  - controlled management of side effects
  - strong business invariants
  - clear and consistent global structure
  - business data validation
  - data persistence

---

## 2. Scope & Limitations

- This project is an application that simulates an anomaly management system within a company.
- Single-user application.
- The application does not handle technical authentication.  
  It operates with a current business actor, selected from predefined roles, used exclusively to apply business rules.
- No networking.
- No real-time processing.
- Business simplifications are intentionally assumed.
- This application is not intended to comply with ISO standards.

---

## 3. Business Domain

- An anomaly cannot be created with invalid data.
- States must follow the anomaly lifecycle (no rollback allowed):  
  **PENDING / CORRECTED / RESOLVED / ARCHIVED**
- A corrected anomaly must be associated with a corrective action.
- A resolved anomaly must have an attached document proving the effect of the corrective action.
- Anomalies must be unique and identifiable by their anomaly ID.
- Invalid data is reported and the requested action is rejected.
- A **RESOLVED** anomaly may be refused during the archiving step.
- A refused anomaly **MUST** create a new anomaly that extends it before being archived.
- A refused anomaly **MUST** store the identifier of the anomaly that extends it.
- An anomaly created as an extension **MUST** store the identifier of the anomaly it extends.
- An archived anomaly is frozen and cannot be modified.

---

## 4. Architecture & Organization

- The domain is independent from the rest of the application.
- The persistence layer and the UI are designed to be interchangeable.
- A dedicated class is responsible for managing a cache used exclusively to provide data to the UI.
- All business requests are handled by the application service, which is responsible for communicating with the domain.
- Value objects are used to manage all evolving fields of an anomaly.

---

## 5. Notable Design Choices

- Immutability by default.
- Side effects restricted to domain boundaries.
- Explicit business exceptions.
- High separation of responsibilities.
- Each class should be easily testable.
- User privilege selection provided as program arguments.

---

## 6. Technical Stack

- Java 17
- SQL
- JDBC
- JavaFX
- JUnit 5

---

## 7. Application Startup

The application is launched using Maven and JavaFX.

```bash
mvn javafx:run --args="--role=<ROLE> --employee-id=<ID>"
```

### Available Arguments

Arguments are used to define the current business actor applied to domain rules.

- `--role`  
  Defines the business role of the current actor.  
  Possible values:
  - `EMPLOYEE`
  - `TEAM_LEAD`
  - `QUALITY`
  - `QUALITY_MANAGER`

- `--employee-id`  
  Unique employee identifier used for business traceability.

### Example

```bash
mvn javafx:run --args="--role=QUALITY --employee-id=12345"
```

> These arguments do not represent an authentication mechanism.  
> They are used solely to define the current business context for domain rule application.

---

## 8. Project Status

- [ ] in design
- [x] under development
- [ ] stabilized
- [ ] completed
