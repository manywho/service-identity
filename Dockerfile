FROM maven:onbuild-alpine

EXPOSE 8080

CMD ["java", "-Xmx600m", "-jar", "/usr/src/app/target/service-identity.jar"]
