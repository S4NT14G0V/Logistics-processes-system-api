FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

COPY mvnw pom.xml ./

COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src ./src

RUN ./mvnw -q clean package -DskipTests


FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S app -G app

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

USER app

ENTRYPOINT ["java", "-jar", "app.jar"]