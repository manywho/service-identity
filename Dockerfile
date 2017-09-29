FROM maven:alpine AS build

WORKDIR /usr/src/app

COPY src src
COPY lombok.config lombok.config
COPY pom.xml pom.xml

RUN mvn clean package

FROM openjdk:jre-alpine

EXPOSE 8080

COPY --from=build /usr/src/app/target/service-identity.jar /usr/src/app/target/service-identity.jar

CMD ["java", "-Xmx600m", "-jar", "/usr/src/app/target/service-identity.jar"]
