package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FormDataTest {

    private FormData formData;

    private JsonNode jsonNode;

    @Before
    public void setUp() throws IOException {
        jsonNode = TestUtils.getJsonNodeFromFile("formDataModel.json");
        formData = new FormData(jsonNode);
    }

    @Test
    public void shouldGetSubmissionReference() {
        assertThat(formData.getSubmissionReference(), is(17L));
    }

    @Test
    public void shouldGetCcdCaseId() {
        assertThat(formData.getCcdCaseId(), is(1535574519543819L));
    }

    @Test
    public void shouldGetRegistry() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree("{\n" +
                "    \"registry\": {\n" +
                "      \"name\": \"Birmingham\",\n" +
                "      \"email\": \"birmingham@email.com\",\n" +
                "      \"address\": \"Line 1 Bham\\nLine 2 Bham\\nLine 3 Bham\\nPostCode Bham\",\n" +
                "      \"sequenceNumber\": 20075\n" +
                "    },\n" +
                "    \"submissionReference\": 17\n" +
                "  }");
        assertThat(formData.getRegistry(), is(equalTo(jsonNode)));
    }

    @Test
    public void shouldGetJson() {
        assertThat(formData.getJson(), is(jsonNode));
    }
}