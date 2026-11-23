# ==========================================
# Build Stage
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper & POM
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Download dependencies offline
RUN ./mvnw dependency:go-offline -B

# Copy source code and build production jar
COPY src src
RUN ./mvnw clean package -DskipTests

# ==========================================
# Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S tradexchange && adduser -S tradexchange -G tradexchange
USER tradexchange

# Copy artifact from builder
COPY --from=builder /app/target/tradexchange-backend-1.0.0.jar app.jar

EXPOSE 5000

ENV PORT=5000 \
    JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
