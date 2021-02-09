FROM azul/zulu-openjdk-alpine:11

WORKDIR /usr/bin
COPY target/dockerunit-test-app.jar .
COPY src/main/resources/application.properties .
EXPOSE 8080 8000
CMD ["java", \
    "-Dproperties.location=/usr/bin/application.properties", \
    "-jar", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000", \
    "/usr/bin/dockerunit-test-app.jar"]

