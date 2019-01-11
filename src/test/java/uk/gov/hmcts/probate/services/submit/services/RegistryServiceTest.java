package uk.gov.hmcts.probate.services.submit.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

@RunWith(MockitoJUnitRunner.class)
public class RegistryServiceTest {

    private JavaMailSenderImpl mailSender;
    private ObjectMapper mapper;
    private RegistryService registryService;
    private long submissionReference;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mailSender = mock(JavaMailSenderImpl.class);
        mapper = new ObjectMapper();
        registryService = new RegistryService(mailSender, mapper);
        submissionReference = 1234;
    }

    @Test
    public void populateRegistryResubmitDataNewApplication() throws IOException {
        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataResubmitNewApplication.json");
        JsonNode formData = TestUtils.getJsonNodeFromFile("formData.json");
        JsonNode response = registryService.populateRegistryData(submissionReference, formData);
        assertEquals(response.toString(), registryData.toString());
    }

    @Test
    public void populateRegistryResubmitDataOldApplication() throws IOException {
        Properties messageProperties = new Properties();
        messageProperties.put("recipient", "oxford@email.com");
        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataResubmitOldApplication.json");
        JsonNode formData = TestUtils.getJsonNodeFromFile("formDataOldApplication.json");
        when(mailSender.getJavaMailProperties()).thenReturn(messageProperties);

        JsonNode response = registryService.populateRegistryData(submissionReference, formData);
        assertEquals(response.toString(), registryData.toString());
    }
}
