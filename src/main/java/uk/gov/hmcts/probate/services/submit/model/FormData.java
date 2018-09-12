package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;

public class FormData {

    private final JsonNode json;

    public FormData(JsonNode json) {
        this.json = json;
    }

    public Long getSubmissionReference() {
        return json.at("/submissionReference").asLong();
    }

    public Long getCcdCaseId(){
        return json.at("/ccdCase/id").asLong();
    }

    public JsonNode getRegistry(){
        return json.at("/registry");
    }

    public JsonNode getJson() {
        return json;
    }
}
