package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class PaymentResponseTest {

    private PaymentResponse paymentResponse;

    private PaymentResponse noPaymentResponse;

    @Before
    public void setUp() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(TestUtils.getJSONFromFile("paymentResponse.json"));
        paymentResponse = new PaymentResponse(jsonNode);

        JsonNode noPaymentjsonNode = objectMapper.readTree(TestUtils.getJSONFromFile("noPaymentResponse.json"));
        noPaymentResponse = new PaymentResponse(noPaymentjsonNode);
    }

    @Test
    public void shouldGetAmount() {
        assertThat(paymentResponse.getAmount(), is(36500L));
    }

    @Test
    public void shouldGetDateCreated() {
        assertThat(paymentResponse.getDateCreated(), is("2018-09-05T11:09:05.227+0000"));
    }

    @Test
    public void shouldGetReference() {
        assertThat(paymentResponse.getReference(), is("RC-1536-1457-4509-0641"));
    }

    @Test
    public void shouldGetStatus() {
        assertThat(paymentResponse.getStatus(), is("Success"));
    }

    @Test
    public void shouldGetStatusForNoPayment() {
        assertThat(noPaymentResponse.getStatus(), is(nullValue()));
    }

    @Test
    public void shouldGetChannel() {
        assertThat(paymentResponse.getChannel(), is("online"));
    }

    @Test
    public void shouldGetTransactionId() {
        assertThat(paymentResponse.getTransactionId(), is("r4jb083f4pi6g8chhcnmb2gsa3"));
    }

    @Test
    public void shouldGetSiteId() {
        assertThat(paymentResponse.getSiteId(), is("P223"));
    }

}
