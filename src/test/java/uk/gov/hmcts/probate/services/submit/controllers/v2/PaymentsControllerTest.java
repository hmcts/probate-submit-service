package uk.gov.hmcts.probate.services.submit.controllers.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.probate.services.submit.services.PaymentsService;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CasePayment;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.ProbatePaymentDetails;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
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

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void shouldUpdatePaymentByCaseId() throws Exception {
        String json = TestUtils.getJSONFromFile("files/v2/intestacyGrantOfRepresentation_caseDetails.json");
        ProbateCaseDetails caseDetailsRequest = objectMapper.readValue(json, ProbateCaseDetails.class);

        mockMvc.perform(post(UPDATE_CASE_URL + "/" + CASE_ID)
            .content(objectMapper.writeValueAsString(caseDetailsRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(paymentsService).updateCaseByCaseId(eq(CASE_ID), eq(caseDetailsRequest));
    }

    @Test
    public void shouldAddPaymentToCase() throws Exception {
        String json = TestUtils.getJSONFromFile("files/v2/intestacyGrantOfRepresentation.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.PA_APP_CREATED);
        CasePayment payment = grantOfRepresentation.getPayments().get(0).getValue();
        ProbateCaseDetails caseResponse = ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();
        ProbatePaymentDetails paymentUpdateRequest = ProbatePaymentDetails.builder()
            .payment(payment)
            .caseType(CaseType.GRANT_OF_REPRESENTATION)
            .build();
        when(paymentsService.addPaymentToCase(eq(EMAIL_ADDRESS), eq(paymentUpdateRequest))).thenReturn(caseResponse);

        mockMvc.perform(post(PAYMENTS_URL + "/" + EMAIL_ADDRESS)
            .content(objectMapper.writeValueAsString(paymentUpdateRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(paymentsService).addPaymentToCase(eq(EMAIL_ADDRESS), eq(paymentUpdateRequest));
    }

    @Test
    public void shouldCreateCase() throws Exception {
        String json = TestUtils.getJSONFromFile("files/v2/intestacyGrantOfRepresentation.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.PA_APP_CREATED);
        ProbateCaseDetails caseResponse = ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();
        when(paymentsService.createCase(eq(EMAIL_ADDRESS), eq(caseResponse))).thenReturn(caseResponse);

        mockMvc.perform(post(PAYMENTS_URL + "/" + EMAIL_ADDRESS + "/" + CREATE_CASES_ENDPOINT)
            .content(objectMapper.writeValueAsString(caseResponse))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(paymentsService).createCase(eq(EMAIL_ADDRESS), eq(caseResponse));
    }
}