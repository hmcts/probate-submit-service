package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SubmitDataTest {

    private SubmitData submitData;

    private JsonNode jsonNode;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        jsonNode = TestUtils.getJsonNodeFromFile("submitdata.json");
        submitData = new SubmitData(jsonNode);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldGetApplicantEmailAddress() {
        assertThat(submitData.getApplicantEmailAddress(), is("nigel@haworthconsulting.co.uk"));
    }

    @Test
    public void shouldGetPayloadVersion() {
        assertThat(submitData.getPayloadVersion(), is("4.1.0"));
    }

    @Test
    public void shouldGetNoOfExecutors() {
        assertThat(submitData.getNoOfExecutors(), is("7"));
    }

    @Test
    public void shouldGetSubmitData() {
        assertThat(submitData.getSubmitData(), is(jsonNode.at("/submitdata")));
    }

    @Test
    public void shouldGetPaymentResponse() {
        assertThat(submitData.getPaymentResponse(), is(notNullValue()));
    }

    @Test
    public void shouldGetPaymentTotal() {
        assertThat(submitData.getPaymentTotal(), is(216.5D));
    }

    @Test
    public void shouldGetCaseId() {
        assertThat(submitData.getCaseId(), is(111111113L));
    }

    @Test
    public void shouldGetCaseState() {
        assertThat(submitData.getCaseState(), is("CaseCreated"));
    }

    @Test
    public void shouldGetRegistry() throws IOException {
        JsonNode jsonNode = objectMapper.readTree("{\n" +
                "    \"registry\": {\n" +
                "      \"name\": \"Birmingham\",\n" +
                "      \"address\": \"Line 1 Bham\\nLine 2 Bham\\nLine 3 Bham\\nPostCode Bham\"\n" +
                "    }\n" +
                "  }");
        assertThat(submitData.getRegistry(), is(equalTo(jsonNode)));
    }

    @Test
    public void shouldGetJson() {
        assertThat(submitData.getJson(), is(jsonNode));
    }

    @Test
    public void shouldEqualsCorrectly() {
        assertThat(submitData, is(equalTo(new SubmitData(jsonNode))));
    }

    @Test
    public void shouldEqualsCorrectlyWhenSameObject() {
        assertThat(submitData, is(equalTo(submitData)));
    }

    @Test
    public void shouldNotEqualWhenDifferentObjectTypes() {
        assertThat(submitData, is(not(equalTo(new String("1234")))));
    }

    @Test
    public void shouldRetrieveHashCode() {
        assertThat(submitData.hashCode(), is(notNullValue()));
    }
}
