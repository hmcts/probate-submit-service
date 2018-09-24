package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Calendar;

import uk.gov.hmcts.probate.services.submit.model.FormData;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;

public class CcdCreateCaseParams {

    private SubmitData submitData;

    private String userId;

    private String authorization;

    private FormData formData;

    private JsonNode submissionReference;

    private JsonNode registryData;

    private Calendar submissionTimestamp;

    public SubmitData getSubmitData() {
        return submitData;
    }

    public String getUserId() {
        return userId;
    }

    public String getAuthorization() {
        return authorization;
    }

    public FormData getFormData() {
        return formData;
    }

    public JsonNode getSubmissionReference() {
        return submissionReference;
    }

    public JsonNode getRegistryData() {
        return registryData;
    }

    public Calendar getSubmissionTimestamp() {
        return submissionTimestamp;
    }

    private CcdCreateCaseParams() {
    }

    public static class Builder {

        private SubmitData submitData;

        private String userId;

        private String authorization;

        private FormData formData;

        private JsonNode submissionReference;

        private JsonNode registryData;

        private Calendar submissionTimestamp;

        public CcdCreateCaseParams build() {
            CcdCreateCaseParams ccdCreateCaseParams = new CcdCreateCaseParams();
            ccdCreateCaseParams.userId = userId;
            ccdCreateCaseParams.submitData = submitData;
            ccdCreateCaseParams.authorization = authorization;
            ccdCreateCaseParams.formData = formData;
            ccdCreateCaseParams.submissionReference = submissionReference;
            ccdCreateCaseParams.registryData = registryData;
            ccdCreateCaseParams.submissionTimestamp = submissionTimestamp;
            return ccdCreateCaseParams;
        }

        public Builder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder withAuthorisation(String authorisation) {
            this.authorization = authorisation;
            return this;
        }

        public Builder withSubmitData(SubmitData submitData) {
            this.submitData = submitData;
            return this;
        }

        public Builder withFormData(FormData formData) {
            this.formData = formData;
            return this;
        }

        public Builder withSubmissionReference(JsonNode submissionReference) {
            this.submissionReference = submissionReference;
            return this;
        }

        public Builder withRegistryData(JsonNode registryData) {
            this.registryData = registryData;
            return this;
        }

        public Builder withSubmissionTimestamp(Calendar submissionTimestamp) {
            this.submissionTimestamp = submissionTimestamp;
            return this;
        }
    }
}
