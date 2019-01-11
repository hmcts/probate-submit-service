package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;

public class PaymentResponse {

    private final JsonNode paymentNode;

    public PaymentResponse(JsonNode paymentNode) {
        this.paymentNode = paymentNode;
    }

    public Long getAmount() {
        JsonNode amountNode = paymentNode.get("amount");
        BigDecimal amount = amountNode == null ? ZERO : new BigDecimal(amountNode.asText());
        return amount.multiply(new BigDecimal(100)).setScale(0).longValue();
    }

    public String getReference() {
        return paymentNode.get("reference").asText();
    }

    public String getDateCreated() {
        return paymentNode.get("date").asText();

    }

    public String getStatus() {
        if (paymentNode.get("status") == null) {
            return null;
        }
        return paymentNode.get("status").asText();
    }

    public String getChannel() {
        return paymentNode.get("channel").asText();
    }

    public String getTransactionId() {
        return paymentNode.get("transactionId").asText();
    }

    public String getSiteId() {
        return paymentNode.get("siteId").asText();
    }

}
