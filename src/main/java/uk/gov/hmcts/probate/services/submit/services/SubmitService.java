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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        FormData formData = persistenceClient.loadFormDataById(submitData.getApplicantEmailAddress());
        if (formData.getSubmissionReference() != 0 && formData.getCcdCaseId() != null) {
            return new TextNode(DUPLICATE_SUBMISSION);
        }
        PersistenceResponse persistenceResponse = persistenceClient.saveSubmission(submitData);
        JsonNode submissionReference = persistenceResponse.getIdAsJsonNode();
        Calendar submissionTimestamp = Calendar.getInstance();
        logger.info(append("tags", "Analytics"), generateMessage(submitData));

        JsonNode registryData = sequenceService.nextRegistry(persistenceResponse.getIdAsLong());

        CcdCreateCaseParams ccdCreateCaseParams = new Builder()
                .withAuthorisation(authorization)
                .withRegistryData(registryData)
                .withSubmissionReference(persistenceResponse.getIdAsJsonNode())
                .withSubmitData(submitData)
                .withUserId(userId)
                .withSubmissionTimestamp(submissionTimestamp)
                .build();

        Optional<CcdCaseResponse> caseResponseOptional = submitCcdCase(ccdCreateCaseParams);
        persistenceClient.updateFormData(submitData.getApplicantEmailAddress(),
                submissionReference.asLong(), formData.getJson());
        ObjectNode response = objectMapper.createObjectNode();
        caseResponseOptional.ifPresent(ccdCase -> addCaseDetailsToFormData(ccdCase, response));
        response.set("registry", registryData);
        response.set("submissionReference", submissionReference);
        return response;
    }

    private String generateMessage(SubmitData submitData) {
        return "Application submitted, payload version: " + submitData.getPayloadVersion()
                + ", number of executors: " + submitData.getNoOfExecutors();
    }

    private void addCaseDetailsToFormData(CcdCaseResponse ccdCaseResponse, ObjectNode response){
        response.set("caseId", new LongNode(ccdCaseResponse.getCaseId()));
        response.set("caseState", new TextNode(ccdCaseResponse.getState()));
        logger.info("submit case - caseId: {}, caseState: {}", ccdCaseResponse.getCaseId(), ccdCaseResponse.getState());
    }

    private Optional<CcdCaseResponse> submitCcdCase(CcdCreateCaseParams ccdCreateCaseParams) {
        if (!coreCaseDataEnabled) {
            return Optional.empty();
        }
        Optional<CcdCaseResponse> caseResponseOptional = coreCaseDataClient
                .getCase(ccdCreateCaseParams.getSubmitData(),
                        ccdCreateCaseParams.getUserId(), ccdCreateCaseParams.getAuthorization());
        return caseResponseOptional.isPresent() ? caseResponseOptional : createCase(ccdCreateCaseParams);
    }

    private Optional<CcdCaseResponse> createCase(CcdCreateCaseParams ccdCreateCaseParams) {
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
        PaymentResponse paymentStatus = submitData.getPaymentResponse();
        persistenceClient.saveSubmission(submitData);
        JsonNode tokenJson = coreCaseDataClient
                .createCaseUpdatePaymentStatusEvent(userId, submitData.getCaseId(), authorization);
        CcdCaseResponse updatePaymentStatusResponse = coreCaseDataClient
                .updatePaymentStatus(submitData.getCaseId(), userId, authorization, tokenJson,
                        paymentStatus);
        Calendar submissionTimestamp = Calendar.getInstance();
        mailClient.execute(submitData.getJson(), submitData.getRegistry(), submissionTimestamp);

        ObjectNode response = objectMapper.createObjectNode();
        response.set("caseState", new TextNode(updatePaymentStatusResponse.getState()));
        logger.info("update payment status - caseId: {}, caseState: {}", updatePaymentStatusResponse.getCaseId(),
                updatePaymentStatusResponse.getState());

        return response;
    }
}
