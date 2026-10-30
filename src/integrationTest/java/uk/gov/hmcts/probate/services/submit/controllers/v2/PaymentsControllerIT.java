package uk.gov.hmcts.probate.services.submit.controllers.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.probate.services.submit.services.PaymentsService;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentsControllerIT {

    private static final String PAYMENTS_URL = "/payments";
    private static final String UPDATE_CASE_URL = "/ccd-case-update";
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String CASE_ID = "1343242352";
    private static final String APPLICATION_CREATED = "PAAppCreated";
    private static final String CREATE_CASES_ENDPOINT = "cases";

    @MockitoBean
    private PaymentsService paymentsService;

    @MockitoBean
    private SecurityUtils securityUtils;

    @MockitoBean
    private CcdDataStoreService ccdDataStoreService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void shouldUpdatePaymentByCaseId() throws Exception {
        String json = TestUtils.getJsonFromFile("files/v2/intestacyGrantOfRepresentation_caseDetails.json");
        ProbateCaseDetails caseDetailsRequest = objectMapper.readValue(json, ProbateCaseDetails.class);

        mockMvc.perform(post(UPDATE_CASE_URL + "/" + CASE_ID)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("caseworker-probate")))
            .content(objectMapper.writeValueAsString(caseDetailsRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(paymentsService).updateCaseByCaseId(eq(CASE_ID), eq(caseDetailsRequest));
    }

    @Test
    public void shouldCreateCase() throws Exception {
        when(securityUtils.getUserId()).thenReturn("test-citizen-user-id");
        when(ccdDataStoreService.isCaseOwner(EMAIL_ADDRESS)).thenReturn(true);
        String json = TestUtils.getJsonFromFile("files/v2/intestacyGrantOfRepresentation.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.PA_APP_CREATED);
        ProbateCaseDetails caseResponse =
            ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();
        when(paymentsService.createCase(eq(EMAIL_ADDRESS), eq(caseResponse))).thenReturn(caseResponse);

        mockMvc.perform(post(PAYMENTS_URL + "/" + EMAIL_ADDRESS + "/" + CREATE_CASES_ENDPOINT)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("citizen")))
            .content(objectMapper.writeValueAsString(caseResponse))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(paymentsService).createCase(eq(EMAIL_ADDRESS), eq(caseResponse));
    }
}
