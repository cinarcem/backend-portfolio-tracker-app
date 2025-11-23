FROM gradle:8.12.1-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY src ./src

RUN gradle build -x test --no-daemon

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

EXPOSE ${PORTFOLIO_SERVICE_PORT}

COPY --from=build /app/build/libs/*.jar app.jar

ENV TZ=Europe/Istanbul
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -Xlog:gc*:stdout:time,uptime,level,tags"

ENTRYPOINT ["java", "-jar", "app.jar"]