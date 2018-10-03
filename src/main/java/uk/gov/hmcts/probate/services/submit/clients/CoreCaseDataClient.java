package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.probate.services.submit.model.CcdCaseResponse;
import uk.gov.hmcts.probate.services.submit.model.PaymentResponse;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;

import java.util.Optional;

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
    private static final String CASE_QUERY_PARAM_PREFIX = "case.";

    public static final String PRIMARY_APPLICANT_EMAIL_ADDRESS_FIELD = "primaryApplicantEmailAddress";
    private static final String APPLICANT_EMAIL = "applicantEmail";
    public static final String STATUS_CODE_LOG = "Status Code: {}";

    @Autowired
    public CoreCaseDataClient(RestTemplate restTemplate, RequestFactory requestFactory,
                              CoreCaseDataMapper ccdMapper) {
        this.restTemplate = restTemplate;
        this.requestFactory = requestFactory;
        this.ccdMapper = ccdMapper;
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public JsonNode createCase(CcdCreateCaseParams ccdCreateCaseParams) {
        String url = UriComponentsBuilder.fromHttpUrl(getBaseUrl(ccdCreateCaseParams.getUserId())).pathSegment(
                EVENT_TRIGGERS_RESOURCE,
                APPLY_FOR_GRANT_CCD_EVENT_ID, TOKEN_RESOURCE).toUriString();
        return getEventToken(ccdCreateCaseParams.getAuthorization(), url);
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public CcdCaseResponse saveCase(CcdCreateCaseParams ccdCreateCaseParams, JsonNode token) {
        JsonNode ccdData = ccdMapper
                .createCcdData(ccdCreateCaseParams.getSubmitData().getSubmitData(),
                        APPLY_FOR_GRANT_CCD_EVENT_ID, token, ccdCreateCaseParams.getSubmissionTimestamp(),
                        ccdCreateCaseParams.getRegistryData());
        HttpEntity<JsonNode> ccdSaveRequest = requestFactory
                .createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization());
        String saveUrl = UriComponentsBuilder.fromHttpUrl(getBaseUrl(ccdCreateCaseParams.getUserId()))
                .pathSegment(CASES_RESOURCE).toUriString();
        logger.info("Save case url: {}", saveUrl);
        return new CcdCaseResponse(postRequestToUrl(ccdSaveRequest, saveUrl));
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public Optional<CcdCaseResponse> getCase(SubmitData submitData, String userId,
                                             String authorization) {
        logger.info("Checking if case already exists in CCD");
        String caseEndpointUrl = UriComponentsBuilder.fromHttpUrl(getBaseUrl(userId)).pathSegment(CASES_RESOURCE).toUriString();

        HttpEntity<JsonNode> request = requestFactory.createCcdStartRequest(authorization);
        String url = generateUrlWithQueryParams(caseEndpointUrl, submitData.getSubmitData());
        try {
            ResponseEntity<JsonNode> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, JsonNode.class);
            ArrayNode caseResponses = (ArrayNode) response.getBody();
            if (caseResponses.size() == 0) {
                logger.info("Existing case not found in CCD");
                return Optional.empty();
            }
            CcdCaseResponse ccdCaseResponse = new CcdCaseResponse(caseResponses.get(0));
            logger.info("Found case in CCD - caseId: {}, caseState: {}", ccdCaseResponse.getCaseId(), ccdCaseResponse.getState());
            return Optional.of(ccdCaseResponse);
        } catch (HttpClientErrorException e) {
            logger.info("Exception while getting a case from CCD", e);
            logger.info(STATUS_CODE_LOG, e.getStatusText());
            throw new HttpClientErrorException(e.getStatusCode());
        }
    }

    private String generateUrlWithQueryParams(String baseUrl, JsonNode submitData) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam(CASE_QUERY_PARAM_PREFIX + PRIMARY_APPLICANT_EMAIL_ADDRESS_FIELD, submitData.get(APPLICANT_EMAIL).textValue())
                .toUriString();
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public JsonNode createCaseUpdatePaymentStatusEvent(String userId, Long caseId,
                                                       String authorization, String eventId) {
        String url = UriComponentsBuilder.fromHttpUrl(getBaseUrl(userId)).pathSegment(CASES_RESOURCE, caseId.toString(),
                EVENT_TRIGGERS_RESOURCE,
                eventId, TOKEN_RESOURCE).toUriString();
        return getEventToken(authorization, url);
    }

    private JsonNode getEventToken(String authorization, String url) {
        logger.info("Start case: {}", url);
        HttpEntity<JsonNode> request = requestFactory.createCcdStartRequest(authorization);
        try {
            ResponseEntity<JsonNode> response = restTemplate
                    .exchange(url, HttpMethod.GET, request, JsonNode.class);
            return response.getBody().get(TOKEN_RESOURCE);
        } catch (HttpClientErrorException e) {
            logger.info("Exception while getting an event token from CCD", e);
            logger.info(STATUS_CODE_LOG, e.getStatusText());
            throw new HttpClientErrorException(e.getStatusCode());
        }
    }

    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public CcdCaseResponse updatePaymentStatus(SubmitData submitData, String userId,
                                        String authorization,
                                        JsonNode token, PaymentResponse paymentResponse, String eventId) {
        String url = UriComponentsBuilder.fromHttpUrl(getBaseUrl(userId)).pathSegment(CASES_RESOURCE, submitData.getCaseId().toString(),
                EVENTS_RESOURCE).toUriString();
        JsonNode ccdData = ccdMapper.updatePaymentStatus(paymentResponse, eventId, token);
        HttpEntity<JsonNode> ccdSaveRequest = requestFactory.createCcdSaveRequest(ccdData, authorization);

        logger.info("Update case payment url: {}", url);
        return new CcdCaseResponse(postRequestToUrl(ccdSaveRequest, url));
    }

    private JsonNode postRequestToUrl(HttpEntity<JsonNode> ccdSaveRequest, String url) {
        try {
            ResponseEntity<JsonNode> response = restTemplate
                    .exchange(url, HttpMethod.POST, ccdSaveRequest, JsonNode.class);
            logResponse(response);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.info("Exception while saving case to CCD", e);
            logger.info(STATUS_CODE_LOG, e.getStatusText());
            throw new HttpClientErrorException(e.getStatusCode());
        }
    }

    private String getBaseUrl(String userId) {
        return String.format(coreCaseDataServiceURL, userId);
    }

    private void logResponse(ResponseEntity<JsonNode> response) {
        logger.info(STATUS_CODE_LOG, response.getStatusCodeValue());
    }
}
