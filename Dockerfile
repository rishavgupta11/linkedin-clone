# Use official Eclipse Temurin JDK (Java 21)
FROM eclipse-temurin:21-jdk

# Set working directory inside the container
WORKDIR /app

# Copy Gradle files and source code
COPY . .

# Build your Spring Boot project
RUN ./gradlew build -x test

# Expose the Spring Boot port
EXPOSE 8080

# Run the application JAR
CMD ["java", "-jar", "build/libs/Linkedin-Clone-0.0.1-SNAPSHOT.jar"]
