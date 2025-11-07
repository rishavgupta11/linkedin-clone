# -------- Stage 1: Build --------
FROM gradle:8.10-jdk21-alpine AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY src src
RUN gradle clean build -x test --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy only the final JAR
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
