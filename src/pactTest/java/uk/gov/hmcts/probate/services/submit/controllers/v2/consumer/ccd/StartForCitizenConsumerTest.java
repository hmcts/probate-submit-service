package uk.gov.hmcts.probate.services.submit.controllers.v2.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.Map;

import static uk.gov.hmcts.reform.probate.pact.dsl.PactDslBuilderForCaseDetailsList.buildStartEventResponseWithEmptyCaseDetails;

public class StartForCitizenConsumerTest extends AbstractProbateSubmitServicePact {

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "probate_submitService")
    RequestResponsePact startForCitizen(PactDslWithProvider builder) throws Exception {
        return builder
            .given("A Start for a Citizen is requested", setUpStateMapForProvider(createEventId))
            .uponReceiving("A Start for a Citizen")
            .path("/citizens/" + caseworkerUsername + "/jurisdictions/"
                + jurisdictionId + "/case-types/"
                + caseType
                + "/event-triggers/"
                + createEventId
                + "/token")
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(buildStartEventResponseWithEmptyCaseDetails(createEventId))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "startForCitizen")
    public void verifyStartEventForCitizen() throws Exception {

        StartEventResponse startEventResponse = coreCaseDataApi.startForCitizen(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, caseworkerUsername, jurisdictionId,
            caseType, createEventId);

        Assertions.assertThat(startEventResponse.getEventId().equals(createEventId));
    }

    @Override
    protected Map<String, Object> setUpStateMapForProvider(String eventId) throws Exception {
        Map<String, Object> caseDataContentMap = super.setUpStateMapForProvider(eventId);
        caseDataContentMap.put(EVENT_ID, createEventId);
        return caseDataContentMap;
    }
}
