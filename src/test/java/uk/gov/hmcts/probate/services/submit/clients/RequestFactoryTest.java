package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import static org.junit.Assert.assertEquals;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.probate.security.SecurityUtils;


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
        assertEquals( MediaType.APPLICATION_JSON, persistenceRequest.getHeaders().getContentType());
    }
    
    @Test
    public void testCreateCcdSaveRequest() throws IOException {
        JsonNode jsonNode = TestUtils.getJsonNodeFromFile("formPayload.json");
        String authorization = "dummyToken";
        HttpEntity<JsonNode> request = requestFactory.createCcdSaveRequest(jsonNode, authorization);

        assertEquals(jsonNode, request.getBody());
        assertEquals(MediaType.APPLICATION_JSON, request.getHeaders().getContentType());
        System.out.println(request.getHeaders().get("Authorization"));
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
