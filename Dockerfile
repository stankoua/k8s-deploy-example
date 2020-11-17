FROM openjdk:11-jre-slim

RUN mkdir -p /opt/app
WORKDIR /opt/app

COPY ./target/k8s-deploy-1.0-SNAPSHOT-jar-with-dependencies.jar ./

ENTRYPOINT ["java","-Djdk.tls.client.protocols=TLSv1.2","-Dlog4j.debug=true","-jar","k8s-deploy-1.0-SNAPSHOT-jar-with-dependencies.jar"]
