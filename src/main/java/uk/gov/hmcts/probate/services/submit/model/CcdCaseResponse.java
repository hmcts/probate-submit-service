package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;

public class CcdCaseResponse {

    private final JsonNode ccdCaseResponseData;

    public CcdCaseResponse(JsonNode ccdCaseResponseData) {
        this.ccdCaseResponseData = ccdCaseResponseData;
    }

    public Long getCaseId() {
        return ccdCaseResponseData.get("id").asLong();
    }
}
