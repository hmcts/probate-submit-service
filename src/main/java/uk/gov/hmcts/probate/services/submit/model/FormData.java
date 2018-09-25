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

    public JsonNode getSubmissionReferenceAsJsonNode() {
        return json.at("/submissionReference");
    }

    public Long getCcdCaseId(){
        return json.at("/ccdCase/id").asLong();
    }

    public String getCcdCaseState(){
        return json.at("/ccdCase/state").asText();
    }

    public JsonNode getRegistry(){
        return json.at("/formdata/registry");
    }

    public JsonNode getJson() {
        return json;
    }
}
