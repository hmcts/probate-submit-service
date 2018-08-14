package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
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
  public JsonNode createCase(String userId, String authorization) {
    String[] createCaseUrlItems = {getBaseUrl(userId), EVENT_TRIGGERS_RESOURCE,
        APPLY_FOR_GRANT_CCD_EVENT_ID, TOKEN_RESOURCE};
    return getEventToken(authorization, createCaseUrlItems);
  }


  @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
  public JsonNode saveCase(JsonNode submitData, String userId, String authorization,
      JsonNode token, Calendar submissionTimestamp, JsonNode sequenceNumber) {
    JsonNode ccdData = ccdMapper
        .createCcdData(submitData, APPLY_FOR_GRANT_CCD_EVENT_ID, token, submissionTimestamp,
            sequenceNumber);
    HttpEntity<JsonNode> persistenceRequest = requestFactory
        .createCcdSaveRequest(ccdData, authorization);
    String saveUrl = String.format(coreCaseDataServiceURL, userId) + "/" + CASES_RESOURCE;
    logger.info("Save case url: " + saveUrl);
    return postRequestToUrl(persistenceRequest, saveUrl);
  }

  @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
  public JsonNode getCase(JsonNode submitData, String userId, String authorization) {
    String caseEndpointUrl = String.format(coreCaseDataServiceURL, userId) + "/" + CASES_RESOURCE;
    HttpEntity<JsonNode> request = requestFactory.createCcdStartRequest(authorization);
    Map<String, String> queryParameterMap = getQueryParameters(submitData);
    ResponseEntity<JsonNode> response = restTemplate
        .exchange(caseEndpointUrl, HttpMethod.GET, request, JsonNode.class, queryParameterMap);
    return response.getBody();
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
    ResponseEntity<JsonNode> response = restTemplate
        .exchange(startUrl, HttpMethod.GET, request, JsonNode.class);
    return response.getBody().get(TOKEN_RESOURCE);
  }

  @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
  public JsonNode updatePaymentStatus(JsonNode ccdCaseResponse, String userId, String authorization,
      JsonNode token, String paymentStatus) {
    String[] urlItems = {getBaseUrl(userId), CASES_RESOURCE, ccdCaseResponse.get("id").asText(), EVENTS_RESOURCE};
    JsonNode caseDataNode = ccdCaseResponse.get("case_data");
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
      logger.info("Exception while saving case", e);
      logger.info("Status Code: ", e.getStatusText());
      logger.info("Body: ", e.getResponseBodyAsString());
    }
    return null;
  }

  private String getBaseUrl(String userId) {
    return String.format(coreCaseDataServiceURL, userId);
  }

  private void logResponse(ResponseEntity<JsonNode> response) {
    logger.info("Status code: " + response.getStatusCodeValue());
    logger.info("Response body:" + response.toString());
  }
}
