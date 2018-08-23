package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FormData {

    private final JsonNode formData;

    public FormData(JsonNode formData) {
        this.formData = formData;
    }

    public Long getSubmissionReference() {
        return formData.at("/submissionReference").asLong();
    }

    public void setCaseId(Long id) {
        ( (ObjectNode) formData.get("formdata")).put("caseId", id);
    }

    public JsonNode getJson() {
        return formData;
    }
}
