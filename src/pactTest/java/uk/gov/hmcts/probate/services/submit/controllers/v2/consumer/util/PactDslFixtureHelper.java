package uk.gov.hmcts.probate.services.submit.controllers.v2.consumer.util;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.Map;

public class PactDslFixtureHelper {

    private PactDslFixtureHelper() {
    }

    @Value("${ccd.jurisdictionid}")
    String jurisdictionId;

    @Value("${ccd.casetype}")
    String caseType;

    @Value("${ccd.eventid.create}")
    static String createEventId;

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";


    private static final String VALID_PAYLOAD_PATH = "json/probate-casedata-map.json";

    public static CaseDataContent getCaseDataContent(String eventId, String validPayloadPath) throws Exception {

        final String caseData = ResourceLoader.loadJson(validPayloadPath);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(createEventId)
            .token(SOME_AUTHORIZATION_TOKEN)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(eventId)
                    .summary("probateSummary")
                    .description("probate")
                    .build()
            ).data(ObjectMapperTestUtil.convertStringToObject(caseData, Map.class))
            .build();

        return caseDataContent;
    }


}

