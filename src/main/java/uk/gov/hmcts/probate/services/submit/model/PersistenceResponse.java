package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;

public class PersistenceResponse {

    private final JsonNode persistenceResponse;

    public PersistenceResponse(JsonNode submitData) {
        this.persistenceResponse = submitData;
    }

    public JsonNode getIdAsJsonNode() {
        return persistenceResponse.get("id");
    }

    public Long getIdAsLong() {
        return persistenceResponse.get("id").asLong();
    }
}
