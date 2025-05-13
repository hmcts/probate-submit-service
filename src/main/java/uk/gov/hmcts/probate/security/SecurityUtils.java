package uk.gov.hmcts.probate.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.NoSecurityContextException;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Component
public class SecurityUtils {

    private final AuthTokenGenerator authTokenGenerator;
    private static final String BEARER = "Bearer ";

    @Autowired
    public SecurityUtils(final AuthTokenGenerator authTokenGenerator) {
        this.authTokenGenerator = authTokenGenerator;
    }

    public HttpHeaders authorizationHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", authTokenGenerator.generate());
        return headers;
    }

    public SecurityDto getSecurityDto() {
        return SecurityDto.builder()
                .authorisation(getUserToken())
                .userId(getUserId())
                .serviceAuthorisation(generateServiceToken())
                .build();
    }

    public String getUserToken() {
        checkSecurityContext();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getBearerToken((String) authentication.getCredentials());
    }

    public String getUserId() {
        checkSecurityContext();
        return ((ServiceAndUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

    private void checkSecurityContext() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new NoSecurityContextException();
        }
    }

    public String generateServiceToken() {
        return authTokenGenerator.generate();
    }

    public String getBearerToken(String token) {
        if (StringUtils.isBlank(token)) {
            return token;
        }

        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }
}
