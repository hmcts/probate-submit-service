FROM gradle:jdk8 as requestBuilder

COPY . /home/gradle/src
USER root
RUN chown -R gradle:gradle /home/gradle/src
USER gradle

WORKDIR /home/gradle/src
RUN gradle assemble

FROM openjdk:8-alpine

RUN mkdir -p /usr/local/bin

COPY docker/entrypoint.sh /
COPY --from=requestFactory /home/gradle/src/build/libs/submit-service.jar /submit-service.jar

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy= curl --silent --fail http://localhost:8181/health

EXPOSE 8181

ENTRYPOINT [ "/entrypoint.sh" ]
