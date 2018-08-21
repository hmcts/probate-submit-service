package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.probate.services.submit.model.CcdCaseResponse;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;

@Component
public class CoreCaseDataClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${services.coreCaseData.url}")
    private String coreCaseDataServiceURL;

    private RestTemplate restTemplate;
    private RequestFactory requestFactory;
    private CoreCaseDataMapper ccdMapper;

    private static final String APPLY_FOR_GRANT_CCD_EVENT_ID = "applyForGrant";
    private static final String EVENT_TRIGGERS_RESOURCE = "event-triggers";
    private static final String EVENTS_RESOURCE = "events";
    private static final String TOKEN_RESOURCE = "token";
    private static final String CASES_RESOURCE = "cases";
    private static final String UPDATE_PAYMENT_STATUS_CCD_EVENT_ID = "updatePaymentStatus";
    private static final String CASE_QUERY_PARAM_PREFIX = "case.";
    private static final List<String> GET_QUERY_PARAMETERS_FOR_CASE = Arrays
            .asList("primaryApplicantEmailAddress", "deceasedSurname", "deceasedForenames",
                    "deceasedDateOfDeath");

    @Autowired
    public CoreCaseDataClient(RestTemplate restTemplate, RequestFactory requestFactory,
                              CoreCaseDataMapper ccdMapper) {
        this.restTemplate = restTemplate;
        this.requestFactory = requestFactory;
        this.ccdMapper = ccdMapper;
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public JsonNode createCase(CcdCreateCaseParams ccdCreateCaseParams) {
        String[] createCaseUrlItems = {getBaseUrl(ccdCreateCaseParams.getUserId()),
                EVENT_TRIGGERS_RESOURCE,
                APPLY_FOR_GRANT_CCD_EVENT_ID, TOKEN_RESOURCE};
        return getEventToken(ccdCreateCaseParams.getAuthorization(), createCaseUrlItems);
    }


    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public CcdCaseResponse saveCase(CcdCreateCaseParams ccdCreateCaseParams, JsonNode token) {
        JsonNode ccdData = ccdMapper
                .createCcdData(ccdCreateCaseParams.getSubmitData().getSubmitData(),
                        APPLY_FOR_GRANT_CCD_EVENT_ID, token, ccdCreateCaseParams.getSubmissionTimestamp(),
                        ccdCreateCaseParams.getSubmissionReference());
        HttpEntity<JsonNode> ccdSaveRequest = requestFactory
                .createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization());
        String saveUrl = String.format(coreCaseDataServiceURL, ccdCreateCaseParams.getUserId()) + "/"
                + CASES_RESOURCE;
        logger.info("Save case url: " + saveUrl);
        return new CcdCaseResponse(postRequestToUrl(ccdSaveRequest, saveUrl));
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public Optional<CcdCaseResponse> getCase(SubmitData submitData, String userId,
                                             String authorization) {
        String caseEndpointUrl = String.format(coreCaseDataServiceURL, userId) + "/" + CASES_RESOURCE;
        HttpEntity<JsonNode> request = requestFactory.createCcdStartRequest(authorization);
        Map<String, String> queryParameterMap = getQueryParameters(submitData.getSubmitData());
        try {
            ResponseEntity<JsonNode> response = restTemplate
                    .exchange(caseEndpointUrl, HttpMethod.GET, request, JsonNode.class, queryParameterMap);
            ArrayNode caseResponses = (ArrayNode) response.getBody();
            if (caseResponses.size() == 0) {
                return Optional.empty();
            }
            return Optional.of(new CcdCaseResponse(caseResponses.get(0)));
        } catch (HttpClientErrorException e) {
            logger.info("Exception while getting a case from CCD", e);
            logger.info("Status Code: ", e.getStatusText());
            logger.info("Body: ", e.getResponseBodyAsString());
            throw new HttpClientErrorException(e.getStatusCode());
        }
    }

    private Map<String, String> getQueryParameters(JsonNode submitData) {
        return GET_QUERY_PARAMETERS_FOR_CASE.stream()
                .collect(Collectors.toMap(field -> CASE_QUERY_PARAM_PREFIX + field,
                        field -> submitData.get(field).asText()));
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public JsonNode createCaseUpdatePaymentStatusEvent(String userId, String caseId,
                                                       String authorization) {
        String[] createCaseUrlItems = {getBaseUrl(userId), CASES_RESOURCE, caseId,
                EVENT_TRIGGERS_RESOURCE,
                UPDATE_PAYMENT_STATUS_CCD_EVENT_ID, TOKEN_RESOURCE};
        return getEventToken(authorization, createCaseUrlItems);
    }

    private JsonNode getEventToken(String authorization, String[] urlItems) {
        String startUrl = StringUtils.join(urlItems, '/');
        logger.info("Start case: " + startUrl);
        HttpEntity<JsonNode> request = requestFactory.createCcdStartRequest(authorization);
        try {
            ResponseEntity<JsonNode> response = restTemplate
                    .exchange(startUrl, HttpMethod.GET, request, JsonNode.class);
            return response.getBody().get(TOKEN_RESOURCE);
        } catch (HttpClientErrorException e) {
            logger.info("Exception while getting an event token from CCD", e);
            logger.info("Status Code: ", e.getStatusText());
            logger.info("Body: ", e.getResponseBodyAsString());
            throw new HttpClientErrorException(e.getStatusCode());
        }
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public JsonNode updatePaymentStatus(CcdCaseResponse ccdCaseResponse, String userId,
                                        String authorization,
                                        JsonNode token, String paymentStatus) {
        String[] urlItems = {getBaseUrl(userId), CASES_RESOURCE, ccdCaseResponse.getCaseId(),
                EVENTS_RESOURCE};
        JsonNode caseDataNode = ccdCaseResponse.getCaseData();
        JsonNode ccdData = ccdMapper
                .updatePaymentStatus(caseDataNode, paymentStatus, UPDATE_PAYMENT_STATUS_CCD_EVENT_ID,
                        token);
        HttpEntity<JsonNode> ccdSaveRequest = requestFactory
                .createCcdSaveRequest(ccdData, authorization);

        String url = StringUtils.join(urlItems, '/');
        logger.info("Update case payment url: " + url);
        return postRequestToUrl(ccdSaveRequest, url);
    }

    private JsonNode postRequestToUrl(HttpEntity<JsonNode> ccdSaveRequest, String url) {
        try {
            ResponseEntity<JsonNode> response = restTemplate
                    .exchange(url, HttpMethod.POST, ccdSaveRequest, JsonNode.class);
            logResponse(response);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.info("Exception while saving case to CCD", e);
            logger.info("Status Code: ", e.getStatusText());
            logger.info("Body: ", e.getResponseBodyAsString());
            throw new HttpClientErrorException(e.getStatusCode());
        }
    }

    private String getBaseUrl(String userId) {
        return String.format(coreCaseDataServiceURL, userId);
    }

    private void logResponse(ResponseEntity<JsonNode> response) {
        logger.info("Status code: " + response.getStatusCodeValue());
        logger.info("Response body:" + response.toString());
    }
}
