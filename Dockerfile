FROM eclipse-temurin:21-jre
LABEL authors="avinashbasani"

WORKDIR /app

COPY /target/mercury-order-service-0.0.1-SNAPSHOT.jar app.jar

#remember that this is useless and is for documentation only
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]