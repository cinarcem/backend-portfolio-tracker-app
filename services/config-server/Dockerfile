# Build aşaması
FROM gradle:7.6.2-jdk17 AS build
WORKDIR /app

COPY settings.gradle build.gradle ./

COPY src ./src
RUN gradle build -x test

FROM openjdk:17
EXPOSE ${CONFIG_PORT}
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV TZ=Europe/Istanbul
ENTRYPOINT ["java", "-jar", "app.jar"]

