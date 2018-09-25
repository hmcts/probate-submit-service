package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.probate.services.submit.model.FormData;
import uk.gov.hmcts.probate.services.submit.model.PersistenceResponse;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;

@Component
public class PersistenceClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${services.persistence.formdata.url}")
    private String formDataPersistenceUrl;

    @Value("${services.persistence.submissions.url}")
    private String submissionsPersistenceUrl;

    @Value("${services.persistence.sequenceNumber.url}")
    private String sequenceNumberPersistenceUrl;

    private RestTemplate restTemplate;
    private RequestFactory requestFactory;

    @Autowired
    public PersistenceClient(RestTemplate restTemplate, RequestFactory requestFactory) {
        this.restTemplate = restTemplate;
        this.requestFactory = requestFactory;
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public PersistenceResponse saveSubmission(SubmitData submitData) {
        HttpEntity<JsonNode> persistenceRequest = requestFactory.createPersistenceRequest(submitData.getJson());
        HttpEntity<JsonNode> persistenceResponse = restTemplate.postForEntity(submissionsPersistenceUrl, persistenceRequest, JsonNode.class);
        return new PersistenceResponse(persistenceResponse.getBody());
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public JsonNode loadSubmission(long sequenceId) {
        HttpEntity<JsonNode> loadResponse = restTemplate.getForEntity(submissionsPersistenceUrl + "/" + sequenceId, JsonNode.class);
        return loadResponse.getBody();
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public FormData loadFormDataById(String emailId) {
        HttpEntity<JsonNode> loadResponse = restTemplate.getForEntity(formDataPersistenceUrl + "/" + emailId, JsonNode.class);
        return new FormData(loadResponse.getBody());
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
        HttpEntity<JsonNode> persistenceRequest = requestFactory.createPersistenceRequest(persistenceRequestBody);
        try {
            restTemplate.put(formDataPersistenceUrl + "/" + emailId, persistenceRequest);
        } catch (HttpClientErrorException e) {
            logHttpClientErrorException(e);
            throw e;
        }
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public Long getNextSequenceNumber(String registryName) {
        ResponseEntity<Long> response = restTemplate.getForEntity(sequenceNumberPersistenceUrl + "/" + registryName, Long.class);
        return response.getBody();
    }

    private void logHttpClientErrorException(HttpClientErrorException e) {
        logger.error("Exception while talking to probate-persistence-service: ", e);
        logger.error(e.getMessage());
    }
}
