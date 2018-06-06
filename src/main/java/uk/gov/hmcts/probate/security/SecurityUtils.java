package uk.gov.hmcts.probate.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.probate.services.submit.clients.CoreCaseDataMapper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final AuthTokenGenerator authTokenGenerator;
    private final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    @Autowired
    public SecurityUtils(final AuthTokenGenerator authTokenGenerator) {
        this.authTokenGenerator = authTokenGenerator;
    }

    public HttpHeaders authorizationHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        String serviceAuthToken = authTokenGenerator.generate();
        headers.add("ServiceAuthorization", serviceAuthToken);
        logger.info("serviceAuthtoken Generated...." + serviceAuthToken);
        return headers;
    }
}
