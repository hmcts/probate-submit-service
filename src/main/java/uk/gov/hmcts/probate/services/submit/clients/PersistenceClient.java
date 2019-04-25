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
    public FormData loadFormDataById(String emailId) {
        HttpEntity<JsonNode> loadResponse = restTemplate.getForEntity(formDataPersistenceUrl + "/" + emailId, JsonNode.class);
        return new FormData(loadResponse.getBody());
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
