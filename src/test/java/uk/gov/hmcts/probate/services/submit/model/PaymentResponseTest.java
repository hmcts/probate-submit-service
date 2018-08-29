package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PaymentResponseTest {

    private PaymentResponse paymentResponse;

    @Before
    public void setUp() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(TestUtils.getJSONFromFile("paymentV1Response.json"));
        paymentResponse = new PaymentResponse(jsonNode);
    }

    @Test
    public void shouldGetAmount() {
        assertThat(paymentResponse.getAmount(), is(5000L));
    }

    @Test
    public void shouldGetDateCreated() {
        assertThat(paymentResponse.getDateCreated(), is("2018-08-29T15:25:11.920+0000"));
    }

    @Test
    public void shouldGetReference() {
        assertThat(paymentResponse.getReference(), is("CODE4$$$Hill4314$$$CODE5$$$CODE2/100"));
    }

    @Test
    public void shouldGetStatus() {
        assertThat(paymentResponse.getStatus(), is("success"));
    }
}
