# Dockerfile for Resume Agent Workflow System
FROM eclipse-temurin:17-jdk-alpine as build

WORKDIR /app

# Copy maven files
COPY pom.xml .
COPY src ./src

# Build the application
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    apk del maven

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/target/resume-agent-workflow-1.0.0-SNAPSHOT.jar app.jar

# Create data directory
RUN mkdir -p /app/data

# Environment variables
ENV SPRING_AI_OPENAI_API_KEY=""
ENV RESUME_AGENT_STORAGE_BASE_PATH="/app/data"

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
