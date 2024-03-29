package uk.gov.hmcts.probate.services.submit.controllers.v2.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.probate.pact.dsl.ObjectMapperTestUtil;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.probate.services.submit.controllers.v2.consumer.util.AssertionHelper.assertBackOfficeCaseData;
import static uk.gov.hmcts.reform.probate.pact.dsl.PactDslBuilderForCaseDetailsList.buildCaseDetailsDsl;

public class SubmitEventForCaseworkerConsumerTest extends AbstractProbateSubmitServicePact {

    private static final String BASECASE_PAYLOAD_PATH = "json/base-case.json";

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "probate_submitService")
    RequestResponsePact submitEventForCaseWorker(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("A Submit Event for a Caseworker is requested",
                    setUpStateMapForProviderWithCaseData(APPLY_FOR_GRANT))
            .uponReceiving("A Submit Event for a Caseworker")
            .path("/caseworkers/" + caseworkerUsername
                + "/jurisdictions/" + jurisdictionId
                + "/case-types/" + caseType
                + "/cases/" + CASE_ID
                + "/events"
            )
            .query("ignore-warning=true")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                    SOME_SERVICE_AUTHORIZATION_TOKEN)
            .body(ObjectMapperTestUtil.convertObjectToJsonString(getCaseDataContent(PAYMENT_SUCCESS_APP,
                    BASECASE_PAYLOAD_PATH)))
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .status(HttpStatus.SC_CREATED)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildCaseDetailsDsl(CASE_ID, false, false))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "submitEventForCaseWorker")
    public void verifySubmitEventForCaseworker() throws Exception {
        CaseDataContent caseDataContent = getCaseDataContent(PAYMENT_SUCCESS_APP, BASECASE_PAYLOAD_PATH);

        final CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, caseworkerUsername, jurisdictionId, caseType, CASE_ID.toString(),
                true, caseDataContent);

        assertNotNull(caseDetails);
        assertBackOfficeCaseData(caseDetails);

    }

    @Override
    protected Map<String, Object> setUpStateMapForProviderWithCaseData(String eventId) throws Exception {
        Map<String, Object> caseDataContentMap = super.setUpStateMapForProviderWithCaseData(eventId);
        caseDataContentMap.put(EVENT_ID, PAYMENT_SUCCESS_APP);
        return caseDataContentMap;
    }


}