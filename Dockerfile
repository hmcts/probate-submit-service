 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.2
FROM hmctspublic.azurecr.io/base/java:21-distroless
LABEL maintainer="https://github.com/hmcts/probate-submit-service"

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/submit-service.jar /opt/app
EXPOSE 8181
CMD [ "submit-service.jar" ]
