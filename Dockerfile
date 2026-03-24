# Use an official Java 21 environment as the base
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy all your project files into the container
COPY . .

# Make the Maven wrapper executable
RUN chmod +x mvnw

# Build the Spring Boot app inside the container
RUN ./mvnw clean package -DskipTests

# Expose port 8080 for Render to route traffic
EXPOSE 8080

# Run the built JAR file
CMD ["sh", "-c", "java -jar target/*.jar"]