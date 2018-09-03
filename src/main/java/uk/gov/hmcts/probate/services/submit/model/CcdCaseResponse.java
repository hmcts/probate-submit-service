package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;

public class CcdCaseResponse {

    private final JsonNode json;

    public CcdCaseResponse(JsonNode json) {
        this.json = json;
    }

    public Long getCaseId() {
        return json.get("id").asLong();
    }
}
