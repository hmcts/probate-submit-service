package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PersistenceClient {

    @Value("${services.persistence.formdata.url}")
    private String formDataPersistenceUrl;

    @Value("${services.persistence.submissions.url}")
    private String submissionsPersistenceUrl;

    @Value("${services.persistence.sequenceNumber.url}")
    private String sequenceNumberPersistenceUrl;

    private RestTemplate restTemplate;
    private RequestFactory builder;

    @Autowired
    public PersistenceClient(RestTemplate restTemplate, RequestFactory builder) {
        this.restTemplate = restTemplate;
        this.builder = builder;
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public JsonNode saveSubmission(JsonNode submitData) {
        HttpEntity<JsonNode> persistenceRequest = builder.createPersistenceRequest(submitData);
        HttpEntity<JsonNode> persistenceResponse = restTemplate.postForEntity(submissionsPersistenceUrl, persistenceRequest, JsonNode.class);
        return persistenceResponse.getBody();
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public JsonNode loadSubmission(long sequenceId) {
        HttpEntity<JsonNode> loadResponse = restTemplate.getForEntity(submissionsPersistenceUrl + "/" + sequenceId, JsonNode.class);
        return loadResponse.getBody();
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public JsonNode loadFormDataById(String emailId) {
        HttpEntity<JsonNode> loadResponse = restTemplate.getForEntity(formDataPersistenceUrl + "/" + emailId, JsonNode.class);
        return loadResponse.getBody();
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public JsonNode loadFormDataBySubmissionReference(long submissionReference) {
        HttpEntity<JsonNode> loadResponse =
                restTemplate.getForEntity(formDataPersistenceUrl + "/search/findBySubmissionReference?submissionReference=" + submissionReference, JsonNode.class);
        return loadResponse.getBody();
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public void updateFormData(String emailId, long sequenceNumber, JsonNode formData) {
        ObjectNode persistenceRequestBody = new ObjectMapper().createObjectNode();
        persistenceRequestBody.put("submissionReference", sequenceNumber);
        persistenceRequestBody.set("formdata", formData.get("formdata"));
        HttpEntity<JsonNode> persistenceRequest = builder.createPersistenceRequest(persistenceRequestBody);
        restTemplate.put(formDataPersistenceUrl + "/" + emailId, persistenceRequest);
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public Long getNextSequenceNumber(String registryName){
        ResponseEntity<Long> response = restTemplate.getForEntity(sequenceNumberPersistenceUrl + "/" + registryName, Long.class);
        return response.getBody();
    }
}
