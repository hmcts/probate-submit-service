package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CcdCaseResponseTest {

    private CcdCaseResponse ccdCaseResponse;

    @Before
    public void setUp() throws IOException {
        JsonNode jsonNode = TestUtils.getJsonNodeFromFile("ccdCaseResponse.json");
        ccdCaseResponse = new CcdCaseResponse(jsonNode);
    }

    @Test
    public void shouldGetCaseId() {
        Long caseId = ccdCaseResponse.getCaseId();

        assertThat(caseId, is(equalTo(1535578731181993L)));
    }
}
