FROM gradle:8.12.1-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY src ./src

RUN gradle build -x test --no-daemon

FROM openjdk:17-alpine
WORKDIR /app

EXPOSE ${MARKET_DATA_PORT}

COPY --from=build /app/build/libs/*.jar app.jar

ENV TZ=Europe/Istanbul

ENTRYPOINT ["java", "-jar", "app.jar"]