# ==============================================================================
# Etapa 1: Construcción con Maven y JDK 21
# ==============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ==============================================================================
# Etapa 2: Runtime ligero con JRE 21 Alpine y usuario no-root
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S invengest && adduser -S invengest -G invengest
USER invengest

COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 8085

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -Xss512k"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
