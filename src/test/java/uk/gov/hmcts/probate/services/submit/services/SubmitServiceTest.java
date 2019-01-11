package uk.gov.hmcts.probate.services.submit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.probate.services.submit.clients.CoreCaseDataClient;
import uk.gov.hmcts.probate.services.submit.clients.MailClient;
import uk.gov.hmcts.probate.services.submit.clients.PersistenceClient;
import uk.gov.hmcts.probate.services.submit.model.CcdCaseResponse;
import uk.gov.hmcts.probate.services.submit.model.FormData;
import uk.gov.hmcts.probate.services.submit.model.PaymentResponse;
import uk.gov.hmcts.probate.services.submit.model.PersistenceResponse;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.util.Calendar;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubmitServiceTest {

    private static final String USER_ID = "12345";
    private static final Long CASE_ID = 99999L;
    private static final String CASE_STATE = "CaseCreated";
    private static final String CASE_FAILED_STATE = "CasePaymentFailed";
    private static final String PA_APP_CREATED_STATE = "PaAppCreated";
    private static final Long ID = 1L;
    private static final String AUTHORIZATION_TOKEN = "XXXXXX";
    private static final String APPLICANT_EMAIL_ADDRESS = "test@test.com";

    private static final String CREATE_CASE_CCD_EVENT_ID = "createCase";
    private static final String CREATE_CASE_PAYMENT_FAILED_CCD_EVENT_ID = "createCasePaymentFailed";
    private static final String CREATE_CASE_PAYMENT_FAILED_MULTIPLE_CCD_EVENT_ID = "createCasePaymentFailedMultiple";
    private static final String CREATE_CASE_PAYMENT_SUCCESS_CCD_EVENT_ID = "createCasePaymentSuccess";

    private SubmitService submitService;

    @Mock
    private MailClient mockMailClient;

    @Mock
    private PersistenceClient persistenceClient;

    @Mock
    private CoreCaseDataClient coreCaseDataClient;

    @Mock
    private RegistryService registryService;

    @Mock
    private SubmitData submitData;

    @Mock
    private JsonNode submissionReference;

    @Mock
    private JsonNode jsonNode;

    @Mock
    private PersistenceResponse persistenceResponse;

    @Mock
    private CcdCaseResponse ccdCaseResponse;

    @Mock
    private PaymentResponse paymentResponse;

    private ObjectMapper objectMapper;

    private FormData formData;

    private JsonNode registryData;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");
        submitService = new SubmitService(mockMailClient, persistenceClient, coreCaseDataClient,
                registryService, objectMapper);
        ReflectionTestUtils.setField(submitService, "coreCaseDataEnabled", true);

        setupFormData();

        when(submitData.getApplicantEmailAddress()).thenReturn(APPLICANT_EMAIL_ADDRESS);
        when(submitData.getPaymentResponse()).thenReturn(paymentResponse);
        when(submitData.getCaseState()).thenReturn(CASE_STATE);
        when(submitData.getCaseId()).thenReturn(CASE_ID);

        when(persistenceClient.loadFormDataById(APPLICANT_EMAIL_ADDRESS)).thenReturn(formData);
        when(persistenceClient.saveSubmission(submitData)).thenReturn(persistenceResponse);

        when(persistenceResponse.getIdAsJsonNode()).thenReturn(submissionReference);

        when(submissionReference.asLong()).thenReturn(ID);
        when(persistenceClient.getNextRegistry(ID)).thenReturn(registryData);

        when(ccdCaseResponse.getCaseId()).thenReturn(CASE_ID);
        when(ccdCaseResponse.getState()).thenReturn(CASE_STATE);

        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(Optional.empty());
        when(coreCaseDataClient.saveCase(any(), any())).thenReturn(ccdCaseResponse);
        when(coreCaseDataClient.createCase(any())).thenReturn(jsonNode);
        when(coreCaseDataClient.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_CCD_EVENT_ID)).thenReturn(ccdCaseResponse);

        when(ccdCaseResponse.getPaymentReference()).thenReturn("RC-1537-1988-5489-1986");
        when(ccdCaseResponse.getState()).thenReturn(CASE_STATE);

        when(paymentResponse.getAmount()).thenReturn(1000L);
        when(paymentResponse.getStatus()).thenReturn("Success");
        when(paymentResponse.getReference()).thenReturn("RC-1537-1988-5489-1985");
        when(paymentResponse.getReference()).thenReturn("Ref");
    }

    private void setupFormData() {
        ObjectNode formDataObjectNode = objectMapper.createObjectNode();
        formDataObjectNode.set("submissionReference", new LongNode(0L));
        ObjectNode ccdObjectNode = objectMapper.createObjectNode();
        ccdObjectNode.set("id", new LongNode(CASE_ID));
        ccdObjectNode.set("state", new TextNode(CASE_STATE));
        formDataObjectNode.set("ccdCase", ccdObjectNode);
        ObjectNode formDataNode = objectMapper.createObjectNode();
        formDataNode.set("registry", registryData.get("registry"));
        formDataObjectNode.set("formdata", formDataNode);
        formData = new FormData(formDataObjectNode);
    }

    @Test
    public void shouldReturnDuplicateSubmissionWhenSubmissionReferenceExists() {
        ((ObjectNode) formData.getJson()).set("submissionReference", new LongNode(12345L));
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(Optional.empty());

        JsonNode submitResponse = submitService.submit(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse.asText(), is("DUPLICATE_SUBMISSION"));
    }

    @Test
    public void shouldSubmitSuccessfullyWhenCaseAlreadyExists() {
        Optional<CcdCaseResponse> caseResponseOptional = Optional.of(ccdCaseResponse);
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);
        JsonNode submitResponse = submitService.submit(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        assertThat(submitResponse.at("/caseId").longValue(), is(equalTo(CASE_ID)));
        assertThat(submitResponse.at("/caseState").asText(), is(equalTo(CASE_STATE)));
        assertThat(submitResponse.at("/registry"), is(equalTo(registryData.get("registry"))));
        verify(persistenceClient, never()).updateFormData(APPLICANT_EMAIL_ADDRESS, ID, formData.getJson());
        verify(persistenceClient, times(1)).loadFormDataById(APPLICANT_EMAIL_ADDRESS);
        verify(persistenceClient, never()).saveSubmission(submitData);
        verify(coreCaseDataClient, times(1)).getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);
        verify(coreCaseDataClient, never()).createCase(any());
        verify(coreCaseDataClient, never()).saveCase(any(), any());
        verify(persistenceClient, never()).getNextRegistry(ID);
    }

    @Test
    public void shouldSubmitSuccessfullyAfterCreatingCase() {
        JsonNode submitResponse = submitService.submit(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        assertThat(submitResponse.at("/caseId").longValue(), is(equalTo(CASE_ID)));
        assertThat(submitResponse.at("/caseState").asText(), is(equalTo(CASE_STATE)));
        assertThat(submitResponse.at("/registry"), is(equalTo(registryData.get("registry"))));
        verify(persistenceClient, times(2)).updateFormData(APPLICANT_EMAIL_ADDRESS, ID, formData.getJson());
        verify(persistenceClient, times(1)).loadFormDataById(APPLICANT_EMAIL_ADDRESS);
        verify(persistenceClient, times(1)).saveSubmission(submitData);
        verify(coreCaseDataClient, times(1)).getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);
        verify(coreCaseDataClient, times(1)).createCase(any());
        verify(coreCaseDataClient, times(1)).saveCase(any(), any());
        verify(mockMailClient, never()).execute(any(), any(), any());
        verify(persistenceClient, times(1)).getNextRegistry(ID);
    }

    @Test
    public void shouldNotSubmitToCcdIfCcdIsDisabled() {
        ReflectionTestUtils.setField(submitService, "coreCaseDataEnabled", false);

        JsonNode submitResponse = submitService.submit(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        verify(persistenceClient, times(2)).updateFormData(APPLICANT_EMAIL_ADDRESS, ID, formData.getJson());
        verify(persistenceClient, times(1)).loadFormDataById(APPLICANT_EMAIL_ADDRESS);
        verify(persistenceClient, times(1)).saveSubmission(submitData);
        verify(coreCaseDataClient, never()).getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);
        verify(coreCaseDataClient, never()).createCase(any());
        verify(coreCaseDataClient, never()).saveCase(any(), any());
        verify(mockMailClient, never()).execute(any(), any(), any());
        verify(persistenceClient, times(1)).getNextRegistry(ID);
    }

    @Test
    public void shouldSubmitSuccessfullyAfterCreatingCaseAndDoesNotSendEmail() {
        JsonNode submitResponse = submitService.submit(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        verify(persistenceClient, times(2)).updateFormData(APPLICANT_EMAIL_ADDRESS, ID, formData.getJson());
        verify(persistenceClient, times(1)).loadFormDataById(APPLICANT_EMAIL_ADDRESS);
        verify(persistenceClient, times(1)).saveSubmission(submitData);
        verify(coreCaseDataClient, times(1)).getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);
        verify(coreCaseDataClient, times(1)).createCase(any());
        verify(coreCaseDataClient, times(1)).saveCase(any(), any());
        verify(mockMailClient, never()).execute(any(), any(), any());
        verify(persistenceClient, times(1)).getNextRegistry(ID);
    }


    @Test
    public void shouldUpdatePaymentStatusSuccessfullyWhenPaymentResponseStatusSuccess() {
        when(coreCaseDataClient.createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_CCD_EVENT_ID)).thenReturn(jsonNode);
        Optional<CcdCaseResponse> caseResponseOptional = Optional.of(ccdCaseResponse);
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);
        when(coreCaseDataClient.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_CCD_EVENT_ID)).thenReturn(ccdCaseResponse);

        JsonNode submitResponse = submitService.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        verify(coreCaseDataClient, times(1)).createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_CCD_EVENT_ID);
        verify(coreCaseDataClient, times(1)).updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_CCD_EVENT_ID);
        verify(mockMailClient, times(1)).execute(any(), any(), any());
    }

    @Test
    public void shouldUpdatePaymentStatusSuccessfullyWhenPaymentResponseStatusNull() {
        when(paymentResponse.getStatus()).thenReturn(null);

        when(coreCaseDataClient.createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_CCD_EVENT_ID)).thenReturn(jsonNode);
        Optional<CcdCaseResponse> caseResponseOptional = Optional.of(ccdCaseResponse);
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);
        when(coreCaseDataClient.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_CCD_EVENT_ID)).thenReturn(ccdCaseResponse);
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);

        JsonNode submitResponse = submitService.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        verify(coreCaseDataClient, times(1)).createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_CCD_EVENT_ID);
        verify(coreCaseDataClient, times(1)).updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_CCD_EVENT_ID);
        verify(mockMailClient, times(1)).execute(any(), any(), any());
    }

    @Test
    public void shouldUpdatePaymentStatusSuccessfullyWithPaymentNotSuccess() {
        when(paymentResponse.getStatus()).thenReturn("Failed");
        when(submitData.getCaseState()).thenReturn(PA_APP_CREATED_STATE);
        when(ccdCaseResponse.getState()).thenReturn(PA_APP_CREATED_STATE);
        when(coreCaseDataClient.createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_PAYMENT_FAILED_CCD_EVENT_ID)).thenReturn(jsonNode);
        Optional<CcdCaseResponse> caseResponseOptional = Optional.of(ccdCaseResponse);
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);
        when(coreCaseDataClient.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_PAYMENT_FAILED_CCD_EVENT_ID)).thenReturn(ccdCaseResponse);

        JsonNode submitResponse = submitService.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        verify(coreCaseDataClient, times(1)).createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_PAYMENT_FAILED_CCD_EVENT_ID);
        verify(coreCaseDataClient, times(1)).updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_PAYMENT_FAILED_CCD_EVENT_ID);
        verify(mockMailClient, times(1)).execute(any(), any(), any());
    }

    @Test
    public void shouldResubmitWithSuccess() {
        when(persistenceClient.loadSubmission(Long.parseLong("112233"))).thenReturn(jsonNode);
        when(persistenceClient.loadFormDataBySubmissionReference(Long.parseLong("112233"))).thenReturn(objectMapper.createObjectNode());
        when(registryService.populateRegistryData(Long.parseLong("112233"), objectMapper.createObjectNode())).thenReturn(registryData);
        when(mockMailClient.execute(eq(jsonNode), eq(registryData), any(Calendar.class))).thenReturn("12345678");

        String response = submitService.resubmit(Long.parseLong("112233"));

        assertThat(response, is("12345678"));
    }

    @Test
    public void shouldResubmitWithFailureWhenPersistenceClientThrowsException() {
        doThrow(HttpClientErrorException.class).when(persistenceClient).loadSubmission(999);
        String response = submitService.resubmit(Long.parseLong("999"));

        assertThat(response, is("Invalid submission reference entered.  Please enter a valid submission reference."));
    }

    @Test
    public void shouldUpdatePaymentStatusWithCreateCasePaymentFailedMultipleWhenPaymentFailsAgain() {
        when(paymentResponse.getStatus()).thenReturn("Failed");
        when(submitData.getCaseState()).thenReturn(CASE_FAILED_STATE);
        when(ccdCaseResponse.getState()).thenReturn(CASE_FAILED_STATE);
        when(coreCaseDataClient.createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_PAYMENT_FAILED_MULTIPLE_CCD_EVENT_ID)).thenReturn(jsonNode);
        Optional<CcdCaseResponse> caseResponseOptional = Optional.of(ccdCaseResponse);
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);
        when(coreCaseDataClient.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_PAYMENT_FAILED_MULTIPLE_CCD_EVENT_ID)).thenReturn(ccdCaseResponse);

        JsonNode submitResponse = submitService.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        verify(coreCaseDataClient, times(1)).createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_PAYMENT_FAILED_MULTIPLE_CCD_EVENT_ID);
        verify(coreCaseDataClient, times(1)).updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_PAYMENT_FAILED_MULTIPLE_CCD_EVENT_ID);
        verify(mockMailClient, times(1)).execute(any(), any(), any());
    }

    @Test
    public void shouldUpdatePaymentStatusSuccessfullyWhenPaymentSucceedsAfterAFailure() {
        when(submitData.getCaseState()).thenReturn(CASE_FAILED_STATE);
        when(ccdCaseResponse.getState()).thenReturn(CASE_FAILED_STATE);
        when(coreCaseDataClient.createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_PAYMENT_SUCCESS_CCD_EVENT_ID)).thenReturn(jsonNode);
        Optional<CcdCaseResponse> caseResponseOptional = Optional.of(ccdCaseResponse);
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);
        when(coreCaseDataClient.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_PAYMENT_SUCCESS_CCD_EVENT_ID)).thenReturn(ccdCaseResponse);

        JsonNode submitResponse = submitService.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        verify(coreCaseDataClient, times(1)).createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_PAYMENT_SUCCESS_CCD_EVENT_ID);
        verify(coreCaseDataClient, times(1)).updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_PAYMENT_SUCCESS_CCD_EVENT_ID);
        verify(mockMailClient, times(1)).execute(any(), any(), any());
    }

    @Test
    public void shouldNotUpdatePaymentStatusWhenExistingCaseNotFound() {
        Optional<CcdCaseResponse> caseResponseOptional = Optional.empty();
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);

        JsonNode submitResponse = submitService.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(equalTo(objectMapper.createObjectNode())));
        verify(coreCaseDataClient, never()).createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_CCD_EVENT_ID);
        verify(coreCaseDataClient,  never()).updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_CCD_EVENT_ID);
        verify(mockMailClient,  never()).execute(any(), any(), any());
    }


    @Test
    public void shouldNotUpdatePaymentStatusSuccessfullyWhenPaymentReferencesIsTheSameAsExisting() {
        Optional<CcdCaseResponse> caseResponseOptional = Optional.of(ccdCaseResponse);
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);
        when(ccdCaseResponse.getPaymentReference()).thenReturn("RC-1537-1988-5489-1986");
        when(paymentResponse.getReference()).thenReturn("RC-1537-1988-5489-1986");

        JsonNode submitResponse = submitService.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(equalTo(objectMapper.createObjectNode())));
        verify(coreCaseDataClient, never()).createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_CCD_EVENT_ID);
        verify(coreCaseDataClient,  never()).updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_CCD_EVENT_ID);
        verify(mockMailClient,  never()).execute(any(), any(), any());
    }

    @Test
    public void shouldUpdatePaymentStatusSuccessfullyWhenPaymentReferencesIsTheSameAndPaymentIsZero() {
        when(ccdCaseResponse.getState()).thenReturn(CASE_STATE);
        when(coreCaseDataClient.createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_CCD_EVENT_ID)).thenReturn(jsonNode);
        Optional<CcdCaseResponse> caseResponseOptional = Optional.of(ccdCaseResponse);
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);
        when(coreCaseDataClient.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_CCD_EVENT_ID)).thenReturn(ccdCaseResponse);
        when(paymentResponse.getAmount()).thenReturn(0L);

        JsonNode submitResponse = submitService.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        verify(coreCaseDataClient, times(1)).createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, CREATE_CASE_CCD_EVENT_ID);
        verify(coreCaseDataClient,  times(1)).updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse, CREATE_CASE_CCD_EVENT_ID);
        verify(mockMailClient,  times(1)).execute(any(), any(), any());
    }
}
