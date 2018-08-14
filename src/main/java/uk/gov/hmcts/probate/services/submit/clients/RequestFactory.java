package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.security.SecurityUtils;

@Component
public class RequestFactory {
    
    private final SecurityUtils securityUtils;

    @Autowired
    public RequestFactory(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }
    
    public HttpEntity<JsonNode> createPersistenceRequest(JsonNode requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }
        
    public HttpEntity<JsonNode> createCcdSaveRequest(JsonNode requestBody, String authorization) {
        HttpHeaders headers = securityUtils.authorizationHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + authorization);
        return new HttpEntity<>(requestBody, headers);
    }
        
    public HttpEntity<JsonNode> createCcdStartRequest(String authorization) {
        HttpHeaders headers = securityUtils.authorizationHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + authorization);
        return new HttpEntity<>(headers);
    }
}
