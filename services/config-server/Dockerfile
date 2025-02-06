# Build aşaması
FROM gradle:7.6.2-jdk17 AS build
WORKDIR /app

COPY settings.gradle build.gradle ./
RUN gradle dependencies

COPY src ./src
RUN gradle build -x test

FROM openjdk:17-alpine
WORKDIR /app

ENV PORT=8888 \
    TZ=Europe/Istanbul

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE ${PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
