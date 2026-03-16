# Build
FROM gradle:8.11-jdk17-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

COPY --from=builder /app/build/libs/*.jar app.jar
RUN rm -f *-plain.jar 2>/dev/null || true

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]