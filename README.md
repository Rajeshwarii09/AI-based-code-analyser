# Code Analyzer

Spring Boot and Angular application for authenticated Java code uploads and asynchronous static analysis.

## Architecture

1. Angular authenticates with the Spring Boot API and lets the user choose a language.
2. Authenticated uploads are stored in PostgreSQL.
3. The upload is published as JSON to Kafka topic `code-snippet-topic`.
4. The Kafka consumer compiles the source, runs PMD, SpotBugs, and Checkstyle,
   and creates a deterministic AI-style explanation.
5. Findings are stored in `analysis_results`.
6. The Angular client polls for the result and displays status and findings.

## Multi-language support

The analyzer accepts `JAVA`, `PYTHON`, `JAVASCRIPT`, `TYPESCRIPT`, `C`, `CPP`,
`GO`, and `RUST`. Java uses the full PMD, Checkstyle, SpotBugs, and JDK
compiler pipeline. Other languages use a fixed, non-shell compiler command
when the corresponding tool is installed on the backend host, plus common
quality checks such as long-line and TODO detection.

| Language | Syntax/compilation tool |
| --- | --- |
| Python | `python -m py_compile` |
| JavaScript | `node --check` |
| TypeScript | `tsc --noEmit` |
| C | `gcc -fsyntax-only` |
| C++ | `g++ -fsyntax-only` |
| Go | `go tool compile` |
| Rust | `rustc --emit metadata` |

If a non-Java compiler is not installed, the report contains a warning instead
of executing arbitrary code. The service never runs submitted programs.

## Low-Level Design (LLD)

### 1. Component architecture

```text
Angular UI
  ├── App shell (authentication gate and logout)
  ├── LoginComponent (register/login and JWT storage)
  └── UploadSnippetComponent (editor, history, polling, findings)
           │ HTTP + Bearer JWT
           ▼
Spring Boot REST API
  ├── AuthController
  │     └── UserRepository + BCryptPasswordEncoder + JwtUtil
  ├── CodeSnippetController
  │     └── CodeSnippetService
  │           ├── CodeSnippetRepository (PostgreSQL)
  │           └── KafkaProducerService
  └── AnalysisController
        └── CodeAnalysisService (direct synchronous endpoint)
              ├── JavaCompiler
              ├── PMD
              ├── Checkstyle
              ├── SpotBugs
              └── AiAnalysisService (local/mock adapter)

Kafka: code-snippet-topic
  └── ConsumerService
        ├── CodeAnalysisService
        ├── AnalysisResultRepository (PostgreSQL)
        └── CodeSnippetRepository (status updates)
```

### 2. Upload and asynchronous analysis sequence

1. The user signs in through `POST /api/auth/login`; the API returns a JWT.
2. Angular stores the token in `localStorage` and sends it in the
   `Authorization: Bearer <token>` header.
3. `POST /api/code/upload` validates the request (5–200,000 characters),
   resolves the authenticated user, and persists a `CodeSnippet` as `UPLOADED`.
4. `CodeSnippetService` changes the status to `QUEUED` and publishes JSON to
   `code-snippet-topic`:

   ```json
   { "snippet_id": 42, "code": "public class Example {}" }
   ```

5. `ConsumerService` receives the message, marks the row `ANALYZING`, and
   invokes `CodeAnalysisService`.
6. The service creates a unique temporary directory, writes a Java source file,
   compiles it, and captures compiler diagnostics.
7. PMD and Checkstyle inspect source. SpotBugs inspects compiled bytecode only
   when compilation has no errors. Temporary files are deleted in a `finally`
   cleanup path.
8. `AiAnalysisService` derives a summary, quality score, and suggestions from
   the normalized findings. It does not execute submitted code.
9. The combined `AnalysisReport` is serialized into `AnalysisResult.result`,
   the snippet becomes `ANALYZED`, and Angular polls
   `GET /api/analysis/results/{snippetId}` until a result is available.

### 3. Data model

| Entity | Important fields | Purpose |
| --- | --- | --- |
| `User` | `id`, `username`, `email`, `passwordHash` | Authenticated application user |
| `CodeSnippet` | `id`, `userId`, `content`, `uploadedAt`, `status` | Submitted source and lifecycle state |
| `AnalysisResult` | `id`, `snippetId`, `result`, `status`, `analyzedAt` | Persisted JSON report |

`AnalysisReport` is the API/report DTO. It contains `status`, normalized
`issues`, `compilationDiagnostics`, and `aiInsight`. A normalized issue always
has `toolName`, `lineNumber`, `message`, and `severity`.

### 4. Status and failure handling

```text
UPLOADED → QUEUED → ANALYZING → ANALYZED
                 └────────────→ FAILED
```

- A Kafka send failure marks the snippet `FAILED`.
- An analyzer exception creates a failed `AnalysisResult` and marks the
  snippet `FAILED`.
- Compiler errors do not execute SpotBugs; they are returned as diagnostics.
- Angular times out an upload after 10 seconds and resets the button state.
- Kafka producer timeouts prevent an unavailable broker from blocking requests.

### 5. Security and operational boundaries

- All non-auth endpoints require JWT authentication.
- Analysis-result lookup verifies that the requested snippet belongs to the
  authenticated user.
- Passwords are stored with BCrypt; raw passwords are never persisted.
- Submitted Java is compiled but never run.
- Input size is capped at 200,000 characters.
- Kafka runs on `localhost:29092` for the local Docker setup.
- For production, use environment secrets, HTTPS, a restricted CORS allowlist,
  resource quotas, and a sandboxed worker for any future executable analysis.

### 6. API interaction contract

| Method | Endpoint | Behavior |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Create a validated user |
| `POST` | `/api/auth/login` | Return a JWT |
| `POST` | `/api/code/upload` | Persist and queue a snippet |
| `GET` | `/api/code/history` | Return the current user's snippets |
| `GET` | `/api/analysis/results/{id}` | Return the owned snippet's report |
| `POST` | `/api/analysis/code` | Run a report synchronously |

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js and npm
- PostgreSQL database `codeanalyzer`
- Docker Desktop (for Kafka and Zookeeper)

## Configuration

Set these environment variables for non-development environments:

```powershell
$env:KAFKA_BOOTSTRAP_SERVERS = "localhost:29092"
$env:JWT_SECRET = "replace-with-a-long-random-secret-at-least-32-bytes"
$env:SERVER_PORT = "8080"
```

The default database is `localhost:5432/codeanalyzer`. Update
`src/main/resources/application.properties` or provide standard Spring datasource
environment variables for another database.

## Start the services

Start Kafka and Zookeeper:

```powershell
docker compose up -d zookeeper kafka
```

Start the backend:

```powershell
mvn spring-boot:run
```

Start the frontend in another terminal:

```powershell
cd code-analyser-frontend
npm install
npm start
```

Open `http://localhost:4200`.

## API

```text
POST /api/auth/register
POST /api/auth/login
POST /api/code/upload
GET  /api/code/history
GET  /api/analysis/results/{snippetId}
POST /api/analysis/code
```

`POST /api/analysis/code` accepts a JSON request containing `language` and
`code`, and returns a
structured report immediately. Uploads use the same report shape asynchronously
through Kafka:

```json
{
  "status": "COMPLETED",
  "issues": [],
  "compilationDiagnostics": [],
  "aiInsight": {
    "summary": "The snippet compiled successfully and no analyzer violations were found.",
    "qualityScore": 100,
    "suggestions": ["No problems were detected. Keep the code covered by tests and review security-sensitive changes."]
  }
}
```

Request body:

```json
{ "language": "JAVA", "code": "public class Example { public static void main(String[] args) {} }" }
```

The current `AiAnalysisService` is a safe local adapter. It does not send
submitted code to a third party; replace it with an approved model client only
after adding authentication, timeouts, redaction, rate limits, and a data
retention policy.

The analyzer never executes submitted code. It only invokes the JDK compiler
and static-analysis libraries in a temporary directory. Inputs are limited to
200,000 characters, temporary files are deleted after analysis, and SpotBugs
is skipped when compilation has errors.

Protected endpoints require:

```text
Authorization: Bearer <jwt>
```

## Upload status

```text
UPLOADED -> QUEUED -> ANALYZING -> ANALYZED
                              \-> FAILED
```

Inspect backend logs for `ANALYSIS_MESSAGE_SENT`, `ANALYSIS_STARTED`,
`ANALYSIS_COMPLETED`, and `ANALYSIS_FAILED`.

## Validation

```powershell
mvn -DskipTests compile
cd code-analyser-frontend
npm run build
npm test -- --watch=false
```

## Troubleshooting

- If snippets remain `QUEUED`, confirm Kafka is running and the backend uses
  `localhost:29092`.
- If analysis fails, inspect the backend `ANALYSIS_FAILED` log and confirm the
  temporary-file analyzers are available on the classpath.
- If the browser cannot call the API, confirm the backend is running on port
  `8080` and the frontend is running on port `4200`.
