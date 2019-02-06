package uk.gov.hmcts.probate.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.probate.services.submit.services.SubmitService;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest
public class SecurityConfigurationTest {

    private static final String PRINCIPAL = "probate_backend";

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final String AUTHORIZATION = "Authorization";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @MockBean
    private SubjectResolver<Service> serviceResolver;

    @MockBean
    private RequestAuthorizer<User> userRequestAuthorizer;

    @MockBean
    private SubmitService submitService;

    private Service service;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .defaultRequest(get("/").accept(MediaType.TEXT_HTML))
                .build();

        service = new Service(PRINCIPAL);
        when(serviceResolver.getTokenDetails(anyString())).thenReturn(service);

        User user = new User("123", new HashSet<>());
        when(userRequestAuthorizer.authorise(any(HttpServletRequest.class))).thenReturn(user);
    }

    @Test
    public void shouldGetSwaggerUiWithStatusCodeOkAndUnAuthenticated() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().isOk()).andExpect(unauthenticated());
    }

    @Test
    public void shouldNotAuthenticateForSubmitEndpoint() throws Exception {
        mvc.perform(post("/submit")
                .header("UserId", "123")
                .header(AUTHORIZATION, "RANDOM2XXXXX"))
                .andExpect(unauthenticated());
    }

    @Test
    public void shouldNotAuthenticateForPaymentStatusEndpoint() throws Exception {
        mvc.perform(post("/updatePaymentStatus")
                .header("UserId", "123")
                .header(AUTHORIZATION, "RANDOM2XXXXX"))
                .andExpect(unauthenticated());
    }

    @Test
    public void shouldNotAuthenticateForResubmitEndpoint() throws Exception {
        mvc.perform(get("/resubmit/1")).andExpect(status().isOk()).andExpect(unauthenticated());
    }

    @Test
    public void shouldAuthenticateForCasesEndpointWithServiceAndUserAuthorizationHeader() throws Exception {
        mvc.perform(post("/cases/test@test.com")
                .header(SERVICE_AUTHORIZATION, "Bearer xxxxx.yyyyy.zzzzz")
                .header(AUTHORIZATION, "Bearer jddslfjsdlfj"))
                .andExpect(authenticated());
    }

    @Test
    public void shouldAuthenticateForDraftsEndpointWithServiceAndUserAuthorizationHeader() throws Exception {
        mvc.perform(post("/drafts/test@test.com")
                .header(SERVICE_AUTHORIZATION, "Bearer xxxxx.yyyyy.zzzzz")
                .header(AUTHORIZATION, "Bearer jddslfjsdlfj"))
                .andExpect(authenticated());
    }

    @Test
    public void shouldAuthenticateForSubmissionsEndpointWithServiceAndUserAuthorizationHeader() throws Exception {
        mvc.perform(post("/submissions/test@test.com")
                .header(SERVICE_AUTHORIZATION, "Bearer xxxxx.yyyyy.zzzzz")
                .header(AUTHORIZATION, "Bearer jddslfjsdlfj"))
                .andExpect(authenticated());
    }

    @Test
    public void shouldAuthenticateForPaymentsEndpointWithServiceAndUserAuthorizationHeader() throws Exception {
        mvc.perform(post("/payments/test@test.com")
                .header(SERVICE_AUTHORIZATION, "Bearer xxxxx.yyyyy.zzzzz")
                .header(AUTHORIZATION, "Bearer jddslfjsdlfj"))
                .andExpect(authenticated());
    }
}
