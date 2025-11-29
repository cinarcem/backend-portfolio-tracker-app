FROM gradle:8.12.1-jdk17 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle dependencies

COPY src ./src
RUN gradle build -x test

FROM eclipse-temurin:17-jre-jammy
EXPOSE $CONFIG_PORT
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV JAVA_OPTS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75 \
  -XX:+UseG1GC \
  -Xms256m \
  -Xmx512m \
  -Xlog:gc*:stdout:time,uptime,level,tags \
"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

