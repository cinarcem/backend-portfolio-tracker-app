FROM gradle:8.12.1-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY src ./src

RUN gradle build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

EXPOSE ${DISCOVERY_PORT}

COPY --from=build /app/build/libs/*.jar app.jar

ENV TZ=Europe/Istanbul

#update for railway.app metal edge server error
ENV JAVA_TOOL_OPTIONS="-Djdk.internal.platform.metrics.enable=false -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dspring.main.lazy-initialization=true"
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

ENTRYPOINT ["java", "-jar", "app.jar"]


