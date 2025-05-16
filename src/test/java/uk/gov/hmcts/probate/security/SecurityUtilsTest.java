package uk.gov.hmcts.probate.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.NoSecurityContextException;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
public class SecurityUtilsTest {

    private static final String SERVICE_AUTH_TOKEN = "SERVICEAUTH123456";
    private static final String AUTH_TOKEN = "AUTHXXXXXXX45566";
    private static final String USER_ID = "userName";

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SecurityUtils securityUtils;

    @Test
    public void shouldGetSecurityDto() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        ServiceAndUserDetails serviceAndUserDetails =
                new ServiceAndUserDetails(USER_ID, "token1234", Collections.emptyList(), "probate_backend");
        TestSecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(serviceAndUserDetails, AUTH_TOKEN, "ROLE_USER"));

        SecurityDto securityDto = securityUtils.getSecurityDto();

        assertNotNull(securityDto);
        assertEquals("Bearer " + AUTH_TOKEN, securityDto.getAuthorisation());
        assertEquals(SERVICE_AUTH_TOKEN, securityDto.getServiceAuthorisation());
        assertEquals(USER_ID, securityDto.getUserId());
        TestSecurityContextHolder.clearContext();
    }

    @Test
    public void shouldGetAuthorisationHeaders() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        HttpHeaders httpHeaders = securityUtils.authorizationHeaders();

        assertEquals(Arrays.asList(SERVICE_AUTH_TOKEN), httpHeaders.get("ServiceAuthorization"));
    }

    @Test
    public void shouldThrowNoSecurityContextExceptionWhenNoContextSetOnGetUserId() {
        assertThrows(NoSecurityContextException.class, () -> {
            securityUtils.getUserId();
            TestSecurityContextHolder.clearContext();
        });
    }

    @Test
    public void shouldThrowNoSecurityContextExceptionWhenNoContextSetOnGetUserToken() {
        assertThrows(NoSecurityContextException.class, () -> {
            securityUtils.getUserToken();
            TestSecurityContextHolder.clearContext();
        });
    }

    @Test
    void givenTokenIsNullWhenGetBearerTokenThenReturnNull() {
        testGetBearerToken(null, null);
    }

    @Test
    void givenTokenIsBlankWhenGetBearerTokenThenReturnBlank() {
        testGetBearerToken(" ", " ");
    }

    @Test
    void givenTokenDoesNotHaveBearerWhenGetBearerTokenThenReturnWithBearer() {
        testGetBearerToken("TestToken", "Bearer TestToken");
    }

    @Test
    void givenTokenDoesHaveBearerWhenGetBearerTokenThenReturnWithBearer() {
        testGetBearerToken("Bearer TestToken", "Bearer TestToken");
    }

    private void testGetBearerToken(String input, String expected) {
        assertEquals(expected, securityUtils.getBearerToken(input));
    }
}
