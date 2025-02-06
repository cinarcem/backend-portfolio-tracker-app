FROM gradle:7.6.2-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle clean build -x test

FROM openjdk:17
EXPOSE 8888
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV TZ=Europe/Istanbul
ENTRYPOINT ["java", "-jar", "app.jar"]
