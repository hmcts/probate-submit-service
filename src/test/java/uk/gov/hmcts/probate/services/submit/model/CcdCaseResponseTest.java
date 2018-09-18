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

        assertThat(caseId, is(equalTo(1537198819302615L)));
    }

    @Test
    public void shouldGetPaymentReference() {
        String reference = ccdCaseResponse.getPaymentReference();

        assertThat(reference, is(equalTo("RC-1537-1988-5489-1985")));
    }

    @Test
    public void shouldReturnEmptyStringWhenNoPaymentReference() throws IOException {
        JsonNode jsonNode = TestUtils.getJsonNodeFromFile("ccdCaseResponseNoPayments.json");
        ccdCaseResponse = new CcdCaseResponse(jsonNode);

        String reference = ccdCaseResponse.getPaymentReference();

        assertThat(reference, is(equalTo("")));
    }
}
