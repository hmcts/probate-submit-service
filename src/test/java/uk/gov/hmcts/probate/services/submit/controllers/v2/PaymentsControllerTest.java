package uk.gov.hmcts.probate.services.submit.controllers.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.probate.services.submit.services.PaymentsService;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentsControllerTest {

    private static final String PAYMENTS_URL = "/payments";
    private static final String UPDATE_CASE_URL = "/ccd-case-update";
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String CASE_ID = "1343242352";
    private static final String APPLICATION_CREATED = "PAAppCreated";
    private static final String CREATE_CASES_ENDPOINT = "cases";

    @MockBean
    private PaymentsService paymentsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void shouldUpdatePaymentByCaseId() throws Exception {
        String json = TestUtils.getJsonFromFile("files/v2/intestacyGrantOfRepresentation_caseDetails.json");
        ProbateCaseDetails caseDetailsRequest = objectMapper.readValue(json, ProbateCaseDetails.class);

        mockMvc.perform(post(UPDATE_CASE_URL + "/" + CASE_ID)
            .content(objectMapper.writeValueAsString(caseDetailsRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(paymentsService).updateCaseByCaseId(eq(CASE_ID), eq(caseDetailsRequest));
    }

    @Test
    public void shouldCreateCase() throws Exception {
        String json = TestUtils.getJsonFromFile("files/v2/intestacyGrantOfRepresentation.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.PA_APP_CREATED);
        ProbateCaseDetails caseResponse =
            ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();
        when(paymentsService.createCase(eq(EMAIL_ADDRESS), eq(caseResponse))).thenReturn(caseResponse);

        mockMvc.perform(post(PAYMENTS_URL + "/" + EMAIL_ADDRESS + "/" + CREATE_CASES_ENDPOINT)
            .content(objectMapper.writeValueAsString(caseResponse))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(paymentsService).createCase(eq(EMAIL_ADDRESS), eq(caseResponse));
    }
}
