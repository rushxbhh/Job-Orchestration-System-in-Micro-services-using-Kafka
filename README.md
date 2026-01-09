# Job-Orchestration-System-in-Micro-services-using-Kafka

# Distributed Job Orchestration System

A production-ready, scalable microservices-based job scheduler built with Spring Boot, Apache Kafka, and MySQL. This system enables reliable asynchronous task execution across distributed workers with exactly-once delivery guarantees.

## 🎯 Overview

This system orchestrates job execution across three microservices with event-driven architecture, providing:

- **Scheduled Job Execution**: Time-based job scheduling with millisecond precision
- **Idempotent Processing**: Three-layer protection ensuring exactly-once job execution
- **Fault Tolerance**: Automatic retry and failure handling mechanisms
- **Horizontal Scalability**: Multiple worker instances process jobs in parallel
- **State Management**: Comprehensive job lifecycle tracking with state machine validation

## 🏗️ Architecture

```
┌─────────────┐      REST API     ┌─────────────┐
│   Client    │ ─────────────────>│ Job Service │
└─────────────┘                   │   (8080)    │
                                  └──────┬──────┘
                                         │
                                         │ Query Jobs
                                         │
                                         ▼
                                  ┌─────────────┐
                                  │  Scheduler  │
                                  │  Service    │
                                  │   (8081)    │
                                  └──────┬──────┘
                                         │
                                         │ Kafka Events
                                         │
                                         ▼
                                  ┌─────────────┐
                                  │   Apache    │
                                  │   Kafka     │
                                  └──────┬──────┘
                                         │
                                         │ Consume
                                         │
                                         ▼
                                  ┌─────────────┐
                                  │   Worker    │
                                  │  Service    │
                                  │   (8082)    │
                                  └─────────────┘
```

### Services

| Service | Port | Responsibility |
|---------|------|----------------|
| **Job Service** | 8080 | Job CRUD operations, state management, persistence |
| **Scheduler Service** | 8081 | Job scheduling, Kafka event publishing |
| **Worker Service** | 8082 | Job execution, result processing |

## ✨ Key Features

### 1. Three-Layer Idempotency Protection

Ensures exactly-once job execution even under failure scenarios:

- **Layer 1: Consumer Groups** - Kafka ensures each message goes to only one worker
- **Layer 2: Status Validation** - Database checks prevent re-execution of running/completed jobs
- **Layer 3: Optimistic Locking** - JPA `@Version` prevents race conditions on status updates

### 2. State Machine with Validation

Enforces valid job state transitions:

```
CREATED → SCHEDULED → RUNNING → SUCCESS
                               ↘ FAILED → SCHEDULED (retry)
```

Invalid transitions are rejected with clear error messages.

### 3. Event-Driven Architecture

- **Decoupled Services**: Services communicate via REST APIs and Kafka events
- **Asynchronous Processing**: Non-blocking job execution
- **Scalable**: Add more workers without code changes

### 4. Fault Tolerance

- Automatic retry for failed operations
- Manual acknowledgment in Kafka prevents message loss
- Comprehensive error logging and tracking

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.6+
- MySQL 8.0+
- Docker & Docker Compose (for Kafka)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/job-orchestration-system.git
   cd job-orchestration-system
   ```

2. **Set up MySQL Database**
   ```sql
   CREATE DATABASE job_orchestration;
   ```

3. **Start Kafka using Docker**
   ```bash
   docker-compose up -d
   ```

   Or manually:
   ```bash
   # Start Zookeeper
   docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:7.4.0

   # Start Kafka
   docker run -d --name kafka -p 9092:9092 \
     --link zookeeper \
     -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
     -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
     confluentinc/cp-kafka:7.4.0
   ```

4. **Configure application properties**
   
   Update `application.yml` in each service with your database credentials:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/job_orchestration
       username: your_username
       password: your_password
   ```

5. **Build all services**
   ```bash
   # Job Service
   cd job-service
   mvn clean install

   # Scheduler Service
   cd ../scheduler-service
   mvn clean install

   # Worker Service
   cd ../worker-service
   mvn clean install
   ```

6. **Run the services**

   Open three terminal windows:

   ```bash
   # Terminal 1 - Job Service
   cd job-service
   mvn spring-boot:run

   # Terminal 2 - Scheduler Service
   cd scheduler-service
   mvn spring-boot:run

   # Terminal 3 - Worker Service
   cd worker-service
   mvn spring-boot:run
   ```

## 📖 Usage

### Create a Job

```bash
curl -X POST http://localhost:8080/api/v1/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My First Job",
    "jobType": "DUMMY",
    "payload": "optional data",
    "scheduledTime": "2024-01-01T10:00:00"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Job created",
  "data": {
    "jobId": "abc-123-uuid",
    "name": "My First Job",
    "status": "CREATED",
    "jobType": "DUMMY",
    "scheduledTime": "2024-01-01T10:00:00",
    "version": 0
  }
}
```

### Get Job Status

```bash
curl http://localhost:8080/api/v1/jobs/{jobId}
```

### List Job Executions

```bash
curl http://localhost:8082/api/v1/executions/job/{jobId}
```

### Supported Job Types

| Type | Description |
|------|-------------|
| `DUMMY` | Test job that sleeps for 2 seconds |
| `HTTP_CALL` | Makes HTTP request to URL in payload |

**HTTP_CALL Example:**
```json
{
  "name": "API Call Job",
  "jobType": "HTTP_CALL",
  "payload": "{\"url\": \"https://api.example.com/endpoint\"}",
  "scheduledTime": "2024-01-01T10:00:00"
}
```

## 🔧 Configuration

### Job Service (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/job_orchestration
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### Scheduler Service (application.yml)

```yaml
server:
  port: 8081

spring:
  kafka:
    bootstrap-servers: localhost:9092

scheduler:
  scan-interval-ms: 10000  # Scan every 10 seconds
  job-service-url: http://localhost:8080

kafka:
  topics:
    job-execution: job-execution-requests
```

### Worker Service (application.yml)

```yaml
server:
  port: 8082

spring:
  kafka:
    consumer:
      group-id: worker-group
      auto-offset-reset: earliest
      enable-auto-commit: false

worker:
  job-service-url: http://localhost:8080
```

## 📊 API Documentation

### Job Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/jobs` | Create a new job |
| GET | `/api/v1/jobs/{jobId}` | Get job by ID |
| PATCH | `/api/v1/jobs/{jobId}/state` | Update job status |
| GET | `/api/v1/jobs/ready-to-schedule` | Get jobs ready to schedule |
| GET | `/api/v1/jobs/ready-to-run` | Get jobs ready to run |

### Worker Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/executions` | List all executions |
| GET | `/api/v1/executions/job/{jobId}` | Get executions for a job |
| GET | `/api/v1/executions/{executionId}` | Get execution by ID |

### Scheduler Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/scheduler/scan` | Manually trigger job scan |

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

## 🧪 Testing

### End-to-End Test

1. **Create a job with past scheduled time:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/jobs \
     -H "Content-Type: application/json" \
     -d '{
       "name": "E2E Test",
       "jobType": "DUMMY",
       "payload": null,
       "scheduledTime": "2024-01-01T10:00:00"
     }'
   ```

2. **Watch the logs** (all three services)

   **Scheduler logs should show:**
   ```
   Found 1 jobs ready to schedule
   ✓ Job xxx moved to SCHEDULED status
   Found 1 jobs ready to dispatch
   ✓ Job xxx dispatched to Kafka
   ```

   **Worker logs should show:**
   ```
   ✓ Job xxx status updated to RUNNING
   Starting DUMMY job execution...
   DUMMY job completed successfully
   ✓ Job xxx final status: SUCCESS
   ```

3. **Verify job status:**
   ```bash
   curl http://localhost:8080/api/v1/jobs/{jobId}
   ```

   Should show `"status": "SUCCESS"`

### Testing Idempotency

Send the same job ID to Kafka multiple times:

```bash
# The worker will skip duplicate messages
# Check logs for: "Job already RUNNING. Skipping duplicate."
```

### Load Testing

Run multiple workers:

```bash
# Terminal 4
cd worker-service
mvn spring-boot:run -Dserver.port=8083

# Terminal 5
cd worker-service
mvn spring-boot:run -Dserver.port=8084
```

Create multiple jobs and watch them distribute across workers.

## 🏭 Production Considerations

### Deployment

1. **Containerization**
   ```dockerfile
   FROM eclipse-temurin:21-jre
   COPY target/*.jar app.jar
   ENTRYPOINT ["java", "-jar", "/app.jar"]
   ```

2. **Environment Variables**
   ```bash
   export DB_URL=jdbc:mysql://prod-db:3306/jobs
   export KAFKA_BROKERS=kafka-1:9092,kafka-2:9092
   ```

### Monitoring

Add Spring Boot Actuator endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

Monitor:
- Job processing rate
- Execution duration
- Failure rate
- Kafka consumer lag

### Scaling

- **Horizontal Scaling**: Add more worker instances
- **Database**: Use connection pooling, read replicas
- **Kafka**: Increase partitions for higher throughput

## 🛠️ Technology Stack

- **Backend Framework**: Spring Boot 4.0.0
- **Language**: Java 21
- **Message Queue**: Apache Kafka 4.1.1
- **Database**: MySQL 8.0+
- **ORM**: Hibernate/JPA
- **Service Communication**: OpenFeign
- **Build Tool**: Maven

## 📁 Project Structure

```
job-orchestration-system/
├── job-service/
│   ├── src/main/java/com/example/job_service/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── enums/
│   │   └── exception/
│   └── src/main/resources/
│       └── application.yml
├── scheduler-service/
│   ├── src/main/java/com/example/scheduler_service/
│   │   ├── scheduler/
│   │   ├── client/
│   │   ├── dto/
│   │   └── config/
│   └── src/main/resources/
│       └── application.yml
├── worker-service/
│   ├── src/main/java/com/example/worker_service/
│   │   ├── listener/
│   │   ├── executor/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── client/
│   └── src/main/resources/
│       └── application.yml
├── docker-compose.yml
└── README.md
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/yourusername)
- LinkedIn: [Your LinkedIn](https://linkedin.com/in/yourprofile)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Apache Kafka for reliable message streaming
- The open-source community

## 📮 Support

If you have any questions or run into issues, please:
- Open an [Issue](https://github.com/yourusername/job-orchestration-system/issues)
- Start a [Discussion](https://github.com/yourusername/job-orchestration-system/discussions)

---

⭐ Star this repository if you find it helpful!
