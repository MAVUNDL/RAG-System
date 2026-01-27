# Use Amazon Corretto 25 as the base image
FROM amazoncorretto:25

WORKDIR /app

# Copy the built JAR from your target folder
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Re-create your local folder structure inside the container
RUN mkdir -p /app/src/main/resources/static/ECL-Methodology-Disclosures
COPY src/main/resources/static/ECL-Methodology-Disclosures /app/src/main/resources/static/ECL-Methodology-Disclosures

# Expose ports
EXPOSE 8080 8888

# Run with preview features enabled
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]