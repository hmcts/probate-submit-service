package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RequestFactoryTest {

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private RequestFactory requestFactory;

    @Before
    public void setUp() {
        when(securityUtils.authorizationHeaders()).thenReturn(new HttpHeaders());
    }

    @Test
    public void testCreatePersistenceRequest() throws IOException {
        JsonNode jsonNode = TestUtils.getJsonNodeFromFile("formPayload.json");

        HttpEntity<JsonNode> persistenceRequest = requestFactory.createPersistenceRequest(jsonNode);

        assertEquals(persistenceRequest.getBody(), jsonNode);
        assertEquals(MediaType.APPLICATION_JSON, persistenceRequest.getHeaders().getContentType());
    }

    @Test
    public void testCreateCcdSaveRequest() throws IOException {
        JsonNode jsonNode = TestUtils.getJsonNodeFromFile("formPayload.json");
        String authorization = "dummyToken";
        HttpEntity<JsonNode> request = requestFactory.createCcdSaveRequest(jsonNode, authorization);

        assertEquals(jsonNode, request.getBody());
        assertEquals(MediaType.APPLICATION_JSON, request.getHeaders().getContentType());
        List<String> auth = new ArrayList<>();
        auth.add("Bearer dummyToken");
        assertEquals(request.getHeaders().get("Authorization"), auth);

    }

    @Test
    public void testCreateCcdStartRequest() {
        String authorization = "dummyToken";
        HttpEntity<JsonNode> request = requestFactory.createCcdStartRequest(authorization);

        assertEquals(MediaType.APPLICATION_JSON, request.getHeaders().getContentType());
        List<String> auth = new ArrayList<>();
        auth.add("Bearer dummyToken");
        assertEquals(auth, request.getHeaders().get("Authorization"));
    }
}
