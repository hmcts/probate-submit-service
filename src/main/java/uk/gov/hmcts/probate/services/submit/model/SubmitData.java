package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public class SubmitData {

    private final JsonNode json;

    public SubmitData(JsonNode json) {
        this.json = json;
    }

    public String getApplicantEmailAddress() {
        return json.at("/submitdata/applicantEmail").asText();
    }

    public String getPayloadVersion() {
        return json.at("/submitdata/payloadVersion").asText();
    }

    public String getNoOfExecutors() {
        return json.at("/submitdata/noOfExecutors").asText();
    }

    public JsonNode getSubmitData() {
        return json.at("/submitdata");
    }

    public PaymentResponse getPaymentResponse() {
        return new PaymentResponse(json.at("/submitdata/payment"));
    }

    public double getPaymentTotal() {
        return json.at("/submitdata/totalFee").asDouble();
    }

    public Long getCaseId() {
        return json.at("/submitdata/caseId").asLong();
    }

    public String getCaseState() {
        return json.at("/submitdata/caseState").asText();
    }

    public JsonNode getRegistry() {
        return json.at("/submitdata/registry");
    }

    public JsonNode getJson() {
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubmitData)) return false;
        SubmitData that = (SubmitData) o;
        return Objects.equals(json, that.json);
    }

    @Override
    public int hashCode() {
        return Objects.hash(json);
    }
}
