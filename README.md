# Code Analyzer & Feedback System
## Project Overview
The Code Analyzer & Feedback System enables users to upload code snippets, which are then automatically analyzed using AI or static analysis tools. The system provides actionable feedback and keeps track of previous interactions with the same code files, allowing users to view their analysis history and improvements over time.
---
## Minimum Viable Product (MVP) Features
- Secure user authentication and registration with JWT-based stateless login.
- REST API to accept and store uploaded code snippets.
- Backend analysis service that asynchronously analyzes uploaded code using AI or static tools.
- API to retrieve both current and historical feedback linked to each code snippet.
- Support for tracking and displaying previous interactions and feedback for the same code.
- Basic UI planned for future phases to enable code upload and feedback viewing.
---
## User Stories
- As a user, I want to securely register and log in, so my code and feedback remain private.
- As a user, I want to upload a code snippet so that it can be analyzed.
- As a user, I want to receive feedback on my code to help improve it.
- As a user, I want to access previous analysis results on the same code to track changes over time.
---
## High-Level System Architecture
- **API Gateway:** Acts as the entry point, routing requests and validating JWT tokens.
- **User Authentication Service:** Manages users and issues JWT tokens.
- **Code Upload Service:** Accepts code snippets and stores them in PostgreSQL.
- **Message Queue (Kafka):** Decouples code upload from analysis services for asynchronous processing.
- **Analysis Service:** Consumes Kafka messages, analyzes code, and stores feedback.
- **Result Storage:** PostgreSQL stores user data, code snippets, feedback, and historical analysis.
---
## Technology Stack
| Component              | Technology/Tool             | Reasoning                                           |
|------------------------|-----------------------------|----------------------------------------------------|
| Backend Framework      | Java Spring Boot             | Mature, robust REST API support, microservice friendly |
| Authentication        | JWT with Spring Security     | Stateless, scalable, and industry best practice    |
| Messaging Queue       | Apache Kafka                 | High throughput, scalable, supports event streaming|
| Database              | PostgreSQL                  | Reliable relational DB supporting structured and semi-structured data |
| Version Control       | Git (GitHub)                | Industry standard, supports collaboration and CI/CD |
| Containerization (Future) | Docker + Kubernetes (GKE) | Cloud-native, scalable deployment                   |
| Frontend (Future)      | React or Angular            | Dynamic, modern UI frameworks                        |
---
## Next Steps: Week 2 Plan
### Objectives
- Initialize and configure Git repository.
- Set up a clean project structure for backend services.
- Set up local development environment:
  - Install Java (JDK 11 or higher).
  - Configure IDE (IntelliJ IDEA, Eclipse, or VS Code).
  - Install and configure PostgreSQL.
  - Install and run Apache Kafka.
- Build User Authentication Service:
  - Implement user registration and login endpoints.
  - Integrate JWT-based authentication using Spring Security.
  - Store user data in PostgreSQL.
- Build Code Upload Service:
  - Create REST API endpoints to accept and validate code snippet uploads.
  - Store code snippets in PostgreSQL.
  - Implement Kafka producer to publish messages for new uploads.
- Implement Basic Analysis Service Skeleton:
  - Create a Kafka consumer to listen to new code upload events.
  - Log or simulate code analysis.
  - Prepare to store analysis feedback in the database.


## Technology Stack
| Component              | Technology/Tool             | Reasoning                                           |
|------------------------|-----------------------------|----------------------------------------------------|
| Backend Framework      | Java Spring Boot             | Mature, robust REST API support, microservice friendly |
| Authentication        | JWT with Spring Security     | Stateless, scalable, and industry best practice    |
| Messaging Queue       | Apache Kafka                 | High throughput, scalable, supports event streaming|
| Database              | PostgreSQL                  | Reliable relational DB supporting structured and semi-structured data |
| Version Control       | Git (GitHub)                | Industry standard, supports collaboration and CI/CD |
| Containerization (Future) | Docker + Kubernetes (GKE) | Cloud-native, scalable deployment                   |
| Frontend (Future)      | React or Angular            | Dynamic, modern UI frameworks                        |
