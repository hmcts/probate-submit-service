package uk.gov.hmcts.probate.services.submit.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.StringUtils;

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
        return submitData.at("submitdata");
    }

    public String getPaymentStatus() {
        return submitData.at("/submitdata/payment/status").asText();
    }

    public Float getPaymentDue() {
        String paymentDue = submitData.at("/submitdata/payment/paymentDue").asText();
        return StringUtils.isNumeric(paymentDue) ? Float.valueOf(paymentDue) : 0;
    }

    public String getCaseId() {
        return submitData.at("/submitdata/caseId").asText();
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
