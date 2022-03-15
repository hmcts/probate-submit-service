package uk.gov.hmcts.probate.services.submit.controllers.v2.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.probate.services.submit.controllers.v2.consumer.util.AssertionHelper.assertCaseDetails;
import static uk.gov.hmcts.reform.probate.pact.dsl.PactDslBuilderForCaseDetailsList.buildCaseResourcesDsl;

public class GetCaseConsumerTest extends AbstractProbateSubmitServicePact {

    private static final String BASECASE_PAYLOAD_PATH = "json/base-case.json";

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "probate_submitService")
    RequestResponsePact getCaseDetails(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("A Get Case is requested", setUpStateMapForProviderWithCaseData(createEventId))
            .uponReceiving("A Get for a Case is requested")
            .path("/cases/" + CASE_ID)
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION,
                SOME_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN, "experimental",
                "true")
            .willRespondWith()
            .status(200)
            .body(buildCaseResourcesDsl(100L, false, false))
            .matchHeader(HttpHeaders.CONTENT_TYPE,
                "application/vnd\\.uk\\.gov\\.hmcts\\.ccd-data-store-api\\.case\\.v2\\+json;charset=UTF-8",
                "application/vnd.uk.gov.hmcts.ccd-data-store-api.case.v2+json;charset=UTF-8")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getCaseDetails")
    public void verifyGetCaseDetails() throws Exception {

        caseDataContent = getCaseDataContent(EVENT_ID, BASECASE_PAYLOAD_PATH);

        final CaseDetails caseDetails = coreCaseDataApi.getCase(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, CASE_ID.toString());
        assertThat(caseDetails.getJurisdiction(), is("PROBATE"));
        assertThat(caseDetails.getState(), is(notNullValue()));
        assertCaseDetails(caseDetails);

    }

}
