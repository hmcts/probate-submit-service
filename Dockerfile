ARG APP_INSIGHTS_AGENT_VERSION=2.5.1
FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.2
LABEL maintainer="https://github.com/hmcts/probate-submit-service"
COPY lib/AI-Agent.xml /opt/app/

COPY build/libs/submit-service.jar /opt/app
EXPOSE 8181
CMD [ "submit-service.jar" ]
