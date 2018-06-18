package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {"services.coreCaseData.baseUrl=http://localhost:4451"})
public class CoreCaseDataClientTestTimeout {

    @MockBean
    private PersistenceEntityBuilder entityBuilder;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CoreCaseDataClient coreCaseDataClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(4451);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void TestTimeout() throws Exception {
        HttpEntity<JsonNode> persistenceReq = new HttpEntity<>(new TextNode("requestBody"), new HttpHeaders());
        when(entityBuilder.createCcdStartRequest(any())).thenReturn(persistenceReq);


        stubFor(get(anyUrl()).willReturn(
                aResponse()
                        .withStatus(200)
                        .withFixedDelay(6000)));

        expectedException.expectCause(CoreMatchers.isA(SocketTimeoutException.class));

        JsonNode result = coreCaseDataClient.createCase("", "");

    }
}
