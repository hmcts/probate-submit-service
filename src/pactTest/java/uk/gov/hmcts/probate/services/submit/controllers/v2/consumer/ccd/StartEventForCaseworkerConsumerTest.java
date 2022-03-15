package uk.gov.hmcts.probate.services.submit.controllers.v2.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.Map;

import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.probate.services.submit.controllers.v2.consumer.util.AssertionHelper.assertCaseDetails;
import static uk.gov.hmcts.reform.probate.pact.dsl.PactDslBuilderForCaseDetailsList.buildStartEventReponse;

public class StartEventForCaseworkerConsumerTest extends AbstractProbateSubmitServicePact {

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "probate_submitService")
    RequestResponsePact startEventForCaseWorker(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("A Start Event for a Caseworker is  requested", setUpStateMapForProviderWithCaseData(createEventId))
            .uponReceiving("A Start Event for a Caseworker")
            .path("/caseworkers/" + caseworkerUsername + "/jurisdictions/"
                + jurisdictionId + "/case-types/"
                + caseType
                + "/cases/" + CASE_ID
                + "/event-triggers/"
                + CREATE_APPLICATION_EVENT
                + "/token")
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(buildStartEventReponse(CREATE_APPLICATION_EVENT, "testServiceToken", true,
                true))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "startEventForCaseWorker")
    public void verifyStartEventForCaseworker() throws Exception {

        final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, caseworkerUsername, jurisdictionId,
            caseType, valueOf(CASE_ID), CREATE_APPLICATION_EVENT);

        assertThat(startEventResponse.getEventId(), notNullValue());
        assertCaseDetails(startEventResponse.getCaseDetails());

    }

    @Override
    protected Map<String, Object> setUpStateMapForProviderWithCaseData(String eventId) throws Exception {
        Map<String, Object> caseDataContentMap = super.setUpStateMapForProviderWithCaseData(eventId);
        caseDataContentMap.put(EVENT_ID, CREATE_APPLICATION_EVENT);
        return caseDataContentMap;
    }
}
