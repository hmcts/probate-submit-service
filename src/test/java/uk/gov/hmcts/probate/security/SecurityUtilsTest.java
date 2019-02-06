package uk.gov.hmcts.probate.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.NoSecurityContextException;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class SecurityUtilsTest {

    private static final String SERVICE_AUTH_TOKEN = "SERVICEAUTH123456";
    private static final String AUTH_TOKEN = "AUTHXXXXXXX45566";
    private static final String USER_ID = "userName";

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SecurityUtils securityUtils;

    @Test
    public void shouldGetSecurityDTO() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        ServiceAndUserDetails serviceAndUserDetails =
                new ServiceAndUserDetails(USER_ID, "token1234", Collections.emptyList(), "probate_backend");
        TestSecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(serviceAndUserDetails, AUTH_TOKEN, "ROLE_USER"));

        SecurityDTO securityDTO = securityUtils.getSecurityDTO();

        assertThat(securityDTO, is(notNullValue()));
        assertThat(securityDTO.getAuthorisation(), is("Bearer " + AUTH_TOKEN));
        assertThat(securityDTO.getServiceAuthorisation(), is(SERVICE_AUTH_TOKEN));
        assertThat(securityDTO.getUserId(), is(USER_ID));
        TestSecurityContextHolder.clearContext();
    }

    @Test
    public void shouldGetAuthorisationHeaders() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        HttpHeaders httpHeaders = securityUtils.authorizationHeaders();

        assertThat(httpHeaders.get("ServiceAuthorization"), equalTo(Arrays.asList(SERVICE_AUTH_TOKEN)));
    }

    @Test(expected = NoSecurityContextException.class)
    public void shouldThrowNoSecurityContextExceptionWhenNoContextSetOnGetUserId() {
        securityUtils.getUserId();
        TestSecurityContextHolder.clearContext();
    }

    @Test(expected = NoSecurityContextException.class)
    public void shouldThrowNoSecurityContextExceptionWhenNoContextSetOnGetUserToken() {
        securityUtils.getUserToken();
        TestSecurityContextHolder.clearContext();
    }
}
