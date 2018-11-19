package uk.gov.hmcts.probate.services.submit.services;

import static net.logstash.logback.marker.Markers.append;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.Calendar;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.probate.services.submit.clients.CcdCreateCaseParams;
import uk.gov.hmcts.probate.services.submit.clients.CcdCreateCaseParams.Builder;
import uk.gov.hmcts.probate.services.submit.clients.CoreCaseDataClient;
import uk.gov.hmcts.probate.services.submit.clients.MailClient;
import uk.gov.hmcts.probate.services.submit.clients.PersistenceClient;
import uk.gov.hmcts.probate.services.submit.model.CcdCaseResponse;
import uk.gov.hmcts.probate.services.submit.model.FormData;
import uk.gov.hmcts.probate.services.submit.model.PaymentResponse;
import uk.gov.hmcts.probate.services.submit.model.PersistenceResponse;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;

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
    private MailClient mailClient;
    private PersistenceClient persistenceClient;
    private CoreCaseDataClient coreCaseDataClient;
    private SequenceService sequenceService;
    @Value("${services.coreCaseData.enabled}")
    private boolean coreCaseDataEnabled;
    private ObjectMapper objectMapper;

    @Autowired
    public SubmitService(MailClient mailClient, PersistenceClient persistenceClient,
                         CoreCaseDataClient coreCaseDataClient, SequenceService sequenceService, ObjectMapper objectMapper) {
        this.mailClient = mailClient;
        this.persistenceClient = persistenceClient;
        this.coreCaseDataClient = coreCaseDataClient;
        this.sequenceService = sequenceService;
        this.objectMapper = objectMapper;
    }

    public JsonNode submit(SubmitData submitData, String userId, String authorization) {
        Optional<CcdCaseResponse> caseResponseOptional = getCCDCase(submitData, userId, authorization);
        FormData formData = persistenceClient.loadFormDataById(submitData.getApplicantEmailAddress());
        if (!caseResponseOptional.isPresent()) {
            if (formData.getSubmissionReference() != 0 ) {
                return new TextNode(DUPLICATE_SUBMISSION);
            }
            PersistenceResponse persistenceResponse = persistenceClient.saveSubmission(submitData);
            JsonNode submissionReference = persistenceResponse.getIdAsJsonNode();
            Calendar submissionTimestamp = Calendar.getInstance();
            logger.info(append("tags", "Analytics"), generateMessage(submitData));
            persistenceClient.updateFormData(submitData.getApplicantEmailAddress(),
                    submissionReference.asLong(), formData.getJson());

            JsonNode registryData = sequenceService.nextRegistry(persistenceResponse.getIdAsLong());

            CcdCreateCaseParams ccdCreateCaseParams = new Builder()
                    .withAuthorisation(authorization)
                    .withRegistryData(registryData)
                    .withSubmissionReference(persistenceResponse.getIdAsJsonNode())
                    .withSubmitData(submitData)
                    .withUserId(userId)
                    .withSubmissionTimestamp(submissionTimestamp)
                    .build();

            caseResponseOptional = submitCcdCase(ccdCreateCaseParams);

            updateFormData(formData, submissionReference, registryData);
            caseResponseOptional.ifPresent(ccdCase -> addCaseDetailsToFormData(ccdCase, ((ObjectNode) formData.getJson())));
            persistenceClient.updateFormData(submitData.getApplicantEmailAddress(),
                    submissionReference.asLong(), formData.getJson());
        }
        ObjectNode response = createResponse(caseResponseOptional, formData);
        logger.info("Response on submit: {}", response);
        return response;
    }

    private ObjectNode createResponse(Optional<CcdCaseResponse> caseResponseOptional, FormData formData) {
        ObjectNode response = objectMapper.createObjectNode();
        response.set(REGISTRY_FIELD_NAME, formData.getRegistry());
        response.set("submissionReference", formData.getSubmissionReferenceAsJsonNode());
        setCCDItemsOnResponse(caseResponseOptional, response);
        return response;
    }

    private void updateFormData(FormData formData, JsonNode submissionReference, JsonNode registryData) {
        ((ObjectNode)formData.getJson().get("formdata")).set(REGISTRY_FIELD_NAME, registryData.get(REGISTRY_FIELD_NAME));
        ((ObjectNode) formData.getJson()).set("submissionReference", submissionReference);
        ((ObjectNode) formData.getJson()).set("processState", new TextNode("SUBMIT_SERVICE_SUBMITTED_TO_CCD"));
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

    private void addCaseDetailsToFormData(CcdCaseResponse ccdCaseResponse, ObjectNode response){
        ObjectNode ccdCase = objectMapper.createObjectNode();
        ccdCase.set("id", new LongNode(ccdCaseResponse.getCaseId()));
        ccdCase.set("state", new TextNode(ccdCaseResponse.getState()));
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

    public String resubmit(long submissionId) {
        try {
            JsonNode resubmitData = persistenceClient.loadSubmission(submissionId);
            JsonNode formData = persistenceClient.loadFormDataBySubmissionReference(submissionId);
            JsonNode registryData = sequenceService
                    .populateRegistryResubmitData(submissionId, formData);
            Calendar submissionTimestamp = Calendar.getInstance();
            logger.info("Application re-submitted, registry data payload: " + registryData);
            return mailClient.execute(resubmitData, registryData, submissionTimestamp);
        } catch (HttpClientErrorException e) {
            logger.error("Invalid Submission Reference Exception: ", e);
            return "Invalid submission reference entered.  Please enter a valid submission reference.";
        }
    }

    public JsonNode updatePaymentStatus(SubmitData submitData, String userId, String authorization) {
        PaymentResponse paymentResponse = submitData.getPaymentResponse();
        Optional<CcdCaseResponse> ccdCaseResponse = getCCDCase(submitData, userId, authorization);
        ObjectNode response = objectMapper.createObjectNode();
        if (ccdCaseResponse.isPresent() &&
                ((paymentResponse.getTotal() == 0) || !ccdCaseResponse.get().getPaymentReference().equals(paymentResponse.getReference()))) {
            logger.info("Updating payment status - caseId: {}", submitData.getCaseId());
            persistenceClient.saveSubmission(submitData);

            String eventId = getEventIdFromStatus(paymentResponse, submitData.getCaseState());
            JsonNode tokenJson = coreCaseDataClient
                    .createCaseUpdatePaymentStatusEvent(userId, submitData.getCaseId(), authorization, eventId);
            CcdCaseResponse updatePaymentStatusResponse = coreCaseDataClient
                    .updatePaymentStatus(submitData, userId, authorization, tokenJson,
                            paymentResponse, eventId);
            Calendar submissionTimestamp = Calendar.getInstance();
            mailClient.execute(submitData.getJson(), submitData.getRegistry(), submissionTimestamp);

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
