# --- STAGE 1: Build stage ---
FROM amazoncorretto:25 AS build
WORKDIR /build

# Install Maven manually (since the specific Maven tag is missing)
RUN yum install -y maven

# 1. Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# 2. Package the application (skipping tests)
RUN mvn clean package -DskipTests

# --- STAGE 2: Runtime stage ---
FROM amazoncorretto:25
WORKDIR /app

# 3. Copy the JAR from the 'build' stage
# Make sure the name matches your pom.xml (demo-0.0.1-SNAPSHOT.jar)
COPY --from=build /build/target/demo-0.0.1-SNAPSHOT.jar app.jar

# 4. Re-create your local folder structure for the PDFs
RUN mkdir -p /app/src/main/resources/static/ECL-Methodology-Disclosures
COPY src/main/resources/static/ECL-Methodology-Disclosures /app/src/main/resources/static/ECL-Methodology-Disclosures

# Expose ports
EXPOSE 8080 8888

# Run with preview features
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]