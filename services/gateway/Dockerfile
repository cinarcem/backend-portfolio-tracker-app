FROM gradle:8.12.1-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle dependencies

COPY src ./src
RUN gradle build -x test

FROM openjdk:17-jdk-slim
EXPOSE $GATEWAY_PORT
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
