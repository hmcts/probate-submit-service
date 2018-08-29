package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;

public class PaymentResponse {

    private final JsonNode paymentNode;

    public PaymentResponse(JsonNode paymentNode) {
        this.paymentNode = paymentNode;
    }

    public Long getAmount() {
        return paymentNode.get("amount").asLong();
    }

    public String getReference() {
        return paymentNode.get("reference").asText();
    }

    public String getDateCreated() {
        return paymentNode.get("date_created").asText();

    }

    public String getStatus() {
        return paymentNode.at("/state/status").asText();
    }
}
