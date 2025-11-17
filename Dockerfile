# Используем Eclipse Temurin для лучшей совместимости
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/online-store-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
