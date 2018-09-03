package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public class SubmitData {

    private final JsonNode submitData;

    public SubmitData(JsonNode submitData) {
        this.submitData = submitData;
    }

    public String getApplicantEmailAddress() {
        return submitData.at("/submitdata/applicantEmail").asText();
    }

    public String getPayloadVersion() {
        return submitData.at("/submitdata/payloadVersion").asText();
    }

    public String getNoOfExecutors() {
        return submitData.at("/submitdata/noOfExecutors").asText();
    }

    public JsonNode getSubmitData() {
        return submitData.at("/submitdata");
    }

    public PaymentResponse getPaymentResponse() {
        return new PaymentResponse(submitData.at("/submitdata/paymentResponse"));
    }

    public double getPaymentTotal() {
        return submitData.at("/submitdata/totalFee").asDouble();
    }

    public Long getCaseId() {
        return submitData.at("/submitdata/caseId").asLong();
    }

    public JsonNode getRegistry() {
        return submitData.at("/submitdata/registry");
    }

    public JsonNode getJson() {
        return submitData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubmitData)) return false;
        SubmitData that = (SubmitData) o;
        return Objects.equals(submitData, that.submitData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submitData);
    }
}
