package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;

public class PaymentResponse {

    private final JsonNode paymentNode;

    public PaymentResponse(JsonNode paymentNode) {
        this.paymentNode = paymentNode;
    }

    public boolean isEmpty() {
        return paymentNode.isMissingNode();
    }

    public Long getAmount() {
        return paymentNode.get("amount").asLong() * 100L;
    }

    public String getReference() {
        return paymentNode.get("reference").asText();
    }

    public String getDateCreated() {
        return paymentNode.get("date").asText();

    }

    public String getStatus() {
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
