FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/user-service.jar app.jar

EXPOSE 8083

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
