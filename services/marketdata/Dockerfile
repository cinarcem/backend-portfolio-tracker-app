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

#update for railway.app metal edge server error
ENV JAVA_TOOL_OPTIONS="-Djdk.internal.platform.metrics.enable=false -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --quiet --tries=1 --spider http://localhost:${DISCOVERY_PORT}/actuator/health || exit 1
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

ENTRYPOINT ["java", "-jar", "app.jar"]
