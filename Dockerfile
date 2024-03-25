 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.5.1
FROM hmctspublic.azurecr.io/base/java:17-distroless
LABEL maintainer="https://github.com/hmcts/probate-submit-service"

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/submit-service.jar /opt/app
EXPOSE 8181
CMD [ "submit-service.jar" ]
