package uk.gov.hmcts.probate.services.submit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import uk.gov.hmcts.probate.services.submit.Registry;
import uk.gov.hmcts.probate.services.submit.clients.PersistenceClient;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SequenceServiceTest {

    @Mock
    private Map<Integer, Registry> registryMap;

    @Mock
    private PersistenceClient persistenceClient;

    @Mock
    private JavaMailSenderImpl mailSender;

    @Mock
    private Registry mockRegistry;

    private SequenceService sequenceService;

    private ObjectMapper mapper;

    private static final long SUBMISSION_REFERENCE = 1234L;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        sequenceService = new SequenceService(registryMap, persistenceClient, mailSender, mapper);
    }

    @Test
    public void nextRegistry() throws IOException {
        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");

        Registry registry = new Registry();
        registry.setName("oxford");
        when(persistenceClient.getNextSequenceNumber("oxford")).thenReturn(1234L);
        when(sequenceService.getRegistrySequenceNumber(registry)).thenReturn(20013L);
        registry.setEmail("oxford@email.com");
        registry.setAddress("Test Address Line 1\nTest Address Line 2\nTest Address Postcode");

        JsonNode result = sequenceService.populateRegistrySubmitData(SUBMISSION_REFERENCE, registry);
        assertEquals(result.toString(), registryData.toString());
    }

    @Test
    public void populateRegistrySubmitData() throws IOException {
        when(registryMap.size()).thenReturn(2);
        when(registryMap.get(anyInt())).thenReturn(mockRegistry);

        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");
        when(sequenceService.identifyNextRegistry()).thenReturn(mockRegistry);
        when(mockRegistry.capitalizeRegistryName()).thenReturn("Oxford");
        when(persistenceClient.getNextSequenceNumber("oxford")).thenReturn(1234L);
        when(sequenceService.getRegistrySequenceNumber(mockRegistry)).thenReturn(20013L);
        when(mockRegistry.getEmail()).thenReturn("oxford@email.com");
        when(mockRegistry.getAddress()).thenReturn("Test Address Line 1\nTest Address Line 2\nTest Address Postcode");

        JsonNode response = sequenceService.populateRegistrySubmitData(SUBMISSION_REFERENCE, mockRegistry);
        assertEquals(response.toString(), registryData.toString());
    }

    @Test
    public void populateRegistryResubmitDataNewApplication() throws IOException {
        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataResubmitNewApplication.json");
        JsonNode formData = TestUtils.getJsonNodeFromFile("formData.json");
        JsonNode response = sequenceService.populateRegistryResubmitData(SUBMISSION_REFERENCE, formData);
        assertEquals(response.toString(), registryData.toString());
    }

    @Test
    public void populateRegistryResubmitDataOldApplication() throws IOException {
        Properties messageProperties = new Properties();
        messageProperties.put("recipient", "oxford@email.com");
        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataResubmitOldApplication.json");
        JsonNode formData = TestUtils.getJsonNodeFromFile("formDataOldApplication.json");
        when(mailSender.getJavaMailProperties()).thenReturn(messageProperties);

        JsonNode response = sequenceService.populateRegistryResubmitData(SUBMISSION_REFERENCE, formData);
        assertEquals(response.toString(), registryData.toString());
    }

    @Test
    public void identifyNextRegistry() {
        when(registryMap.size()).thenReturn(2);
        when(registryMap.get(anyInt())).thenReturn(mockRegistry);

        Registry result = sequenceService.identifyNextRegistry();
        assertThat(result, is(equalTo(mockRegistry)));
    }
}
