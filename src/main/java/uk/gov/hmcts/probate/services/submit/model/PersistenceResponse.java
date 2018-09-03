package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;

public class PersistenceResponse {

    private final JsonNode json;

    public PersistenceResponse(JsonNode submitData) {
        this.json = submitData;
    }

    public JsonNode getIdAsJsonNode() {
        return json.get("id");
    }

    public Long getIdAsLong() {
        return json.get("id").asLong();
    }
}
