FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/offshore-proxy-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 9090