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

    public Long getCcdCaseId(){
        return formData.at("/ccdCase/id").asLong();
    }

    public JsonNode getRegistry(){
        return formData.at("/registry");
    }

    public void setSubmissionReference(JsonNode submissionReference) {
        ((ObjectNode) formData.get("formdata")).set("submissionReference", submissionReference);
    }

    public void setRegistry(JsonNode registry) {
        ((ObjectNode) formData.get("formdata")).set("registry", registry);
    }

    public JsonNode getJson() {
        return formData;
    }
}
