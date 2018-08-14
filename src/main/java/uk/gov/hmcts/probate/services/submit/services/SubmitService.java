package uk.gov.hmcts.probate.services.submit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.probate.services.submit.clients.CoreCaseDataClient;
import uk.gov.hmcts.probate.services.submit.clients.MailClient;
import uk.gov.hmcts.probate.services.submit.clients.PersistenceClient;

import static net.logstash.logback.marker.Markers.append;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.HttpClientErrorException;

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

  @Autowired
  public SubmitService(MailClient mailClient, PersistenceClient persistenceClient,
      CoreCaseDataClient coreCaseDataClient, SequenceService sequenceService) {
    this.mailClient = mailClient;
    this.persistenceClient = persistenceClient;
    this.coreCaseDataClient = coreCaseDataClient;
    this.sequenceService = sequenceService;
  }

  public JsonNode submit(JsonNode submitData, String userId, String authorization) {
    String emailId = submitData.at("/submitdata/applicantEmail").asText();
    JsonNode formData = persistenceClient.loadFormDataById(emailId);
    if (formData.get("submissionReference").asLong() == 0) {
      String message = "Application submitted, payload version: " + submitData
          .at("/submitdata/payloadVersion").asText() + ", number of executors: " + submitData
          .at("/submitdata/noOfExecutors").asText();
      JsonNode persistenceResponse = persistenceClient.saveSubmission(submitData);
      JsonNode submissionReference = persistenceResponse.get("id");
      JsonNode registryData = sequenceService.nextRegistry(submissionReference.asLong());
      Calendar submissionTimestamp = Calendar.getInstance();
      mailClient.execute(submitData, registryData, submissionTimestamp);
      logger.info(append("tags", "Analytics"), message);
      if (coreCaseDataEnabled) {
        try {
          ArrayNode ccdCaseResponses = (ArrayNode) coreCaseDataClient
              .getCase(submitData.get("submitdata"), userId, authorization);
          JsonNode ccdCaseResponse;
          if (ccdCaseResponses.size() == 0) {
            JsonNode ccdStartCaseResponse = coreCaseDataClient
                .createCase(userId, authorization);
            ccdCaseResponse = coreCaseDataClient
                .saveCase(submitData.get("submitdata"), userId, authorization,
                    ccdStartCaseResponse, submissionTimestamp, registryData);
          } else {
            ccdCaseResponse = ccdCaseResponses.get(0);
          }
          ((ObjectNode) formData).put("caseId", ccdCaseResponse.get("id").textValue());
          persistenceClient
              .updateFormData(emailId, submissionReference.asLong(), formData);
        } catch (HttpClientErrorException e) {
          logger.error("Exception while talking to ccd: ", e);
          logger.error(e.getMessage());
          logger.error(e.getResponseBodyAsString());
        } catch (Exception e) {
          logger.error("Exception while talking to ccd: ", e);
          logger.error(e.getMessage());
        }
      }
      return registryData;
    }
    return new TextNode(DUPLICATE_SUBMISSION);
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

  public JsonNode updatePaymentStatus(JsonNode submitData, String userId, String authorization) {
    String paymentStatus = submitData.get("paymentStatus").asText();
    JsonNode tokenJson = coreCaseDataClient
        .createCaseUpdatePaymentStatusEvent(userId, submitData.get("caseId").asText(),
            authorization);
    JsonNode ccdCaseResponse = coreCaseDataClient.getCase(submitData, userId, authorization);
    return coreCaseDataClient.updatePaymentStatus(ccdCaseResponse, userId, authorization, tokenJson, paymentStatus);
  }
}