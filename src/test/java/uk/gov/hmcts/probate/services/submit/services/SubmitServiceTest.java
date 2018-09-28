package uk.gov.hmcts.probate.services.submit.services;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.probate.services.submit.clients.MailClient;
import uk.gov.hmcts.probate.services.submit.clients.PersistenceClient;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.gov.hmcts.probate.services.submit.clients.CoreCaseDataClient;

public class SubmitServiceTest {

    private TestUtils testUtils;

    private SubmitService submitService;
    private MailClient mockMailClient;
    private PersistenceClient persistenceClient;
    private CoreCaseDataClient coreCaseDataClient;
    private RegistryService registryService;
    private Calendar submissionTimestamp;
    private JsonNode registryData;

    @Before
    public void setUp() throws Exception {
        testUtils = new TestUtils();
        persistenceClient = mock(PersistenceClient.class);
        mockMailClient = mock(MailClient.class);
        coreCaseDataClient = mock(CoreCaseDataClient.class);
        registryService = mock(RegistryService.class);
        submitService = new SubmitService(mockMailClient, persistenceClient, coreCaseDataClient, registryService);
        submissionTimestamp = Calendar.getInstance();
        registryData = testUtils.getJsonNodeFromFile("registryDataSubmit.json");
    }

    @Test
    public void testSubmitWithSuccess() {
        String userId = "123";
        String authorizationToken = "dummyAuthToken";
        JsonNode submitData = testUtils.getJsonNodeFromFile("formPayload.json");
        when(persistenceClient.loadFormDataById(anyString())).thenReturn(submitData);
        when(persistenceClient.saveSubmission(submitData)).thenReturn(submitData);
        when(mockMailClient.execute(submitData, registryData, submissionTimestamp)).thenReturn("12345678");
        when(persistenceClient.getNextRegistry(submitData.get("id").asLong())).thenReturn(registryData);
        JsonNode dummmyCcdStartCaseRespose =  testUtils.getJsonNodeFromFile("ccdStartCaseResponse.json");

        JsonNode response = submitService.submit(submitData, userId, authorizationToken);

        assertThat(response, is(registryData));
    }

    @Test
    public void testResubmitWithSuccess() {
        JsonNode resubmitData = testUtils.getJsonNodeFromFile("formPayload.json");
        JsonNode formData = testUtils.getJsonNodeFromFile("formData.json");
        JsonNode registryData = testUtils.getJsonNodeFromFile("registryDataResubmitNewApplication.json");
        when(persistenceClient.loadSubmission(Long.parseLong("112233"))).thenReturn(resubmitData);
        when(persistenceClient.loadFormDataBySubmissionReference(Long.parseLong("112233"))).thenReturn(formData);
        when(registryService.populateRegistryData(Long.parseLong("112233"), formData)).thenReturn(registryData);
        when(mockMailClient.execute(eq(resubmitData), eq(registryData), any(Calendar.class) )).thenReturn("12345678");

        String response = submitService.resubmit(Long.parseLong("112233"));

        assertThat(response, is("12345678"));
    }

    @Test
    public void testResubmitWithFailure() {
        doThrow(HttpClientErrorException.class).when(persistenceClient).loadSubmission(999);
        String response = submitService.resubmit(Long.parseLong("999"));
        assertThat(response, is("Invalid submission reference entered.  Please enter a valid submission reference."));
    }
}