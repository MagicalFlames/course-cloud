FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/gateway-service.jar app.jar

EXPOSE 8090

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
