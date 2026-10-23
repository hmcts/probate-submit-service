package uk.gov.hmcts.probate.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.NoSecurityContextException;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    private static final String SERVICE_AUTH_TOKEN = "SERVICEAUTH123456";
    private static final String AUTH_TOKEN = "AUTHXXXXXXX45566";
    private static final String USER_ID = "userName";
    private SecurityUtils securityUtils;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        securityUtils = new SecurityUtils(authTokenGenerator);
    }

    @Test
    void shouldGetSecurityDto() {
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
    void shouldGetAuthorisationHeaders() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        HttpHeaders httpHeaders = securityUtils.authorizationHeaders();

        assertEquals(List.of(SERVICE_AUTH_TOKEN), httpHeaders.get("ServiceAuthorization"));
    }

    @Test
    void shouldThrowNoSecurityContextExceptionWhenNoContextSetOnGetUserId() {
        assertThrows(NoSecurityContextException.class, () -> {
            securityUtils.getUserId();
            TestSecurityContextHolder.clearContext();
        });
    }

    @Test
    void shouldThrowNoSecurityContextExceptionWhenNoContextSetOnGetUserToken() {
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
