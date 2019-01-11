package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class CcdCaseResponse {

    private final JsonNode json;

    public CcdCaseResponse(JsonNode json) {
        this.json = json;
    }

    public Long getCaseId() {
        return json.get("id").asLong();
    }

    public String getState() {
        return json.get("state").asText();
    }

    public String getPaymentReference() {
        JsonNode payments = json.at("/case_data/payments");
        if (payments instanceof ArrayNode && payments.size() > 0) {
            return payments.get(0).at("/value/reference").asText();
        }
        return "";
    }
}
