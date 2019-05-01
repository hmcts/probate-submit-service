package uk.gov.hmcts.probate.services.submit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.probate.services.submit.clients.CcdCreateCaseParams;
import uk.gov.hmcts.probate.services.submit.clients.CcdCreateCaseParams.Builder;
import uk.gov.hmcts.probate.services.submit.clients.CoreCaseDataClient;
import uk.gov.hmcts.probate.services.submit.clients.PersistenceClient;
import uk.gov.hmcts.probate.services.submit.model.CcdCaseResponse;
import uk.gov.hmcts.probate.services.submit.model.FormData;
import uk.gov.hmcts.probate.services.submit.model.PaymentResponse;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;

import java.util.Calendar;
import java.util.Optional;

import static net.logstash.logback.marker.Markers.append;

@Service
public class SubmitService {

    private static final String REGISTRY_FIELD_NAME = "registry";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String CREATE_CASE_CCD_EVENT_ID = "createCase";
    private static final String CREATE_CASE_PAYMENT_FAILED_CCD_EVENT_ID = "createCasePaymentFailed";
    private static final String CREATE_CASE_PAYMENT_FAILED_MULTIPLE_CCD_EVENT_ID = "createCasePaymentFailedMultiple";
    private static final String CREATE_CASE_PAYMENT_SUCCESS_CCD_EVENT_ID = "createCasePaymentSuccess";
    private static final String CASE_PAYMENT_FAILED_STATE = "CasePaymentFailed";
    private static final String DUPLICATE_SUBMISSION = "DUPLICATE_SUBMISSION";
    private PersistenceClient persistenceClient;
    private CoreCaseDataClient coreCaseDataClient;
    private SequenceService sequenceService;
    @Value("${services.coreCaseData.enabled}")
    private boolean coreCaseDataEnabled;
    private ObjectMapper objectMapper;

    @Autowired
    public SubmitService(PersistenceClient persistenceClient,
                         CoreCaseDataClient coreCaseDataClient, SequenceService sequenceService, ObjectMapper objectMapper) {
        this.persistenceClient = persistenceClient;
        this.coreCaseDataClient = coreCaseDataClient;
        this.sequenceService = sequenceService;
        this.objectMapper = objectMapper;
    }

    public JsonNode submit(SubmitData submitData, String userId, String authorization) {
        Optional<CcdCaseResponse> caseResponseOptional = getCCDCase(submitData, userId, authorization);
        FormData formData = persistenceClient.loadFormDataById(submitData.getApplicantEmailAddress());
        if (!caseResponseOptional.isPresent()) {
            logger.info(append("tags", "Analytics"), generateMessage(submitData));
            JsonNode registryData = sequenceService.nextRegistry();

            Calendar submissionTimestamp = Calendar.getInstance();
            CcdCreateCaseParams ccdCreateCaseParams = new Builder()
                    .withAuthorisation(authorization)
                    .withRegistryData(registryData)
                    .withSubmitData(submitData)
                    .withSubmissionTimestamp(submissionTimestamp)
                    .withUserId(userId)
                    .build();
            caseResponseOptional = submitCcdCase(ccdCreateCaseParams);
            caseResponseOptional.ifPresent(ccdCase -> addDetailsToFormData(ccdCase, registryData, formData));
        }
        ObjectNode response = createResponse(caseResponseOptional, formData);
        logger.info("Response on submit: {}", response);
        return response;
    }

    private ObjectNode createResponse(Optional<CcdCaseResponse> caseResponseOptional, FormData formData) {
        ObjectNode response = objectMapper.createObjectNode();
        response.set(REGISTRY_FIELD_NAME, formData.getRegistry());
        setCCDItemsOnResponse(caseResponseOptional, response);
        return response;
    }

    private void setCCDItemsOnResponse(Optional<CcdCaseResponse> caseResponseOptional, ObjectNode response) {
        if (!coreCaseDataEnabled) {
            return;
        }
        if (caseResponseOptional.isPresent()) {
            CcdCaseResponse ccdCaseResponse = caseResponseOptional.get();
            response.set("caseId", new LongNode(ccdCaseResponse.getCaseId()));
            response.set("caseState", new TextNode(ccdCaseResponse.getState()));
        }
    }

    private Optional<CcdCaseResponse> getCCDCase(SubmitData submitData, String userId, String authorization) {
        if (!coreCaseDataEnabled) {
            return Optional.empty();
        }
        return coreCaseDataClient.getCase(submitData, userId, authorization);
    }

    private String generateMessage(SubmitData submitData) {
        return "Application submitted, payload version: " + submitData.getPayloadVersion()
                + ", number of executors: " + submitData.getNoOfExecutors();
    }

    private void addDetailsToFormData(CcdCaseResponse ccdCaseResponse, JsonNode registryData, FormData formData) {
        ((ObjectNode) formData.getJson().get("formdata")).set(REGISTRY_FIELD_NAME, registryData.get(REGISTRY_FIELD_NAME));
        ((ObjectNode) formData.getJson()).set("processState", new TextNode("SUBMIT_SERVICE_SUBMITTED_TO_CCD"));

        ObjectNode ccdCase = objectMapper.createObjectNode();
        ccdCase.set("id", new LongNode(ccdCaseResponse.getCaseId()));
        ccdCase.set("state", new TextNode(ccdCaseResponse.getState()));
        ObjectNode response = (ObjectNode) formData.getJson();
        response.set("ccdCase", ccdCase);
        logger.info("submitted case - caseId: {}, caseState: {}", ccdCaseResponse.getCaseId(), ccdCaseResponse.getState());
    }

    private Optional<CcdCaseResponse> submitCcdCase(CcdCreateCaseParams ccdCreateCaseParams) {
        if (!coreCaseDataEnabled) {
            return Optional.empty();
        }
        JsonNode ccdStartCaseResponse = coreCaseDataClient.createCase(ccdCreateCaseParams);
        return Optional.of(coreCaseDataClient.saveCase(ccdCreateCaseParams, ccdStartCaseResponse));
    }

    public JsonNode updatePaymentStatus(SubmitData submitData, String userId, String authorization) {
        PaymentResponse paymentResponse = submitData.getPaymentResponse();
        Optional<CcdCaseResponse> ccdCaseResponse = getCCDCase(submitData, userId, authorization);
        ObjectNode response = objectMapper.createObjectNode();
        if (ccdCaseResponse.isPresent() &&
                ((paymentResponse.getAmount() == 0) || !ccdCaseResponse.get().getPaymentReference().equals(paymentResponse.getReference()))) {
            logger.info("Updating payment status - caseId: {}", submitData.getCaseId());

            String eventId = getEventIdFromStatus(paymentResponse, submitData.getCaseState());
            JsonNode tokenJson = coreCaseDataClient
                    .createCaseUpdatePaymentStatusEvent(userId, submitData.getCaseId(), authorization, eventId);
            CcdCaseResponse updatePaymentStatusResponse = coreCaseDataClient
                    .updatePaymentStatus(submitData, userId, authorization, tokenJson,
                            paymentResponse, eventId);
            response.set("caseState", new TextNode(updatePaymentStatusResponse.getState()));
            logger.info("Updated payment status - caseId: {}, caseState: {}", updatePaymentStatusResponse.getCaseId(),
                    updatePaymentStatusResponse.getState());
            return response;
        }
        return response;
    }

    private String getEventIdFromStatus(PaymentResponse paymentResponse, String state) {
        if (paymentResponse.getStatus() != null && !paymentResponse.getStatus().equals("Success")) {
            return getFailedPaymentEventIdFromState(state);
        }
        return getSuccessPaymentEventIdFromState(state);
    }

    private String getFailedPaymentEventIdFromState(String state) {
        if (state.equals(CASE_PAYMENT_FAILED_STATE)) {
            return CREATE_CASE_PAYMENT_FAILED_MULTIPLE_CCD_EVENT_ID;
        }
        return CREATE_CASE_PAYMENT_FAILED_CCD_EVENT_ID;
    }

    private String getSuccessPaymentEventIdFromState(String state) {
        if (state.equals(CASE_PAYMENT_FAILED_STATE)) {
            return CREATE_CASE_PAYMENT_SUCCESS_CCD_EVENT_ID;
        }
        return CREATE_CASE_CCD_EVENT_ID;
    }
}
