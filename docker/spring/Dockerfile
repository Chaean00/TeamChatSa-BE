# Build
FROM gradle:8.11-jdk17-jammy AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*.jar /app/
RUN find /app -name "*-plain.jar" -delete && \
    mv /app/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]