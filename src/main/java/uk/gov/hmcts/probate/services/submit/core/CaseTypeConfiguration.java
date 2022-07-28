package uk.gov.hmcts.probate.services.submit.core;

import com.google.common.collect.ImmutableMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

import java.util.Map;

import static uk.gov.hmcts.reform.probate.model.cases.CaseType.CAVEAT;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.GRANT_OF_REPRESENTATION;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.STANDING_SEARCH;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.WILL_LODGEMENT;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.CAVEAT_APPLY_FOR_CAVEAT;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_APPLICATION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_CASE;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_CASE_WITHOUT_PAYMENT;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_DRAFT;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED_AGAIN;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED_TO_SUCCESS;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_UPDATE_APPLICATION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_UPDATE_DRAFT;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.UPDATE_GOP_PAYMENT_FAILED;

@Configuration
public class CaseTypeConfiguration {

    @Bean
    public Map<CaseType, CaseEvents> eventsMap() {
        return ImmutableMap.<CaseType, CaseEvents>builder()
            .put(GRANT_OF_REPRESENTATION, CaseEvents.builder()
                .createCaseApplicationEventId(GOP_CREATE_APPLICATION)
                .updateCaseApplicationEventId(GOP_UPDATE_APPLICATION)
                .createCaseEventId(GOP_CREATE_CASE)
                .createDraftEventId(GOP_CREATE_DRAFT)
                .paymentFailedAgainEventId(GOP_PAYMENT_FAILED_AGAIN)
                .paymentFailedEventId(GOP_PAYMENT_FAILED)
                .paymentFailedToSuccessEventId(GOP_PAYMENT_FAILED_TO_SUCCESS)
                .updateDraftEventId(GOP_UPDATE_DRAFT)
                .createCaseWithoutPaymentId(GOP_CREATE_CASE_WITHOUT_PAYMENT)
                .updatePaymentFailedEventId(UPDATE_GOP_PAYMENT_FAILED)
                .build())
            .put(CAVEAT, CaseEvents.builder()
                .createCaseApplicationEventId(CAVEAT_APPLY_FOR_CAVEAT)
                .createCaseEventId(GOP_CREATE_CASE)
                .paymentFailedAgainEventId(GOP_PAYMENT_FAILED_AGAIN)
                .paymentFailedEventId(GOP_PAYMENT_FAILED)
                .paymentFailedToSuccessEventId(GOP_PAYMENT_FAILED_TO_SUCCESS)
                .build())
            .put(WILL_LODGEMENT, CaseEvents.builder()
                .createCaseApplicationEventId(GOP_CREATE_APPLICATION)
                .createCaseEventId(GOP_CREATE_CASE)
                .createDraftEventId(GOP_CREATE_DRAFT)
                .paymentFailedAgainEventId(GOP_PAYMENT_FAILED_AGAIN)
                .paymentFailedEventId(GOP_PAYMENT_FAILED)
                .paymentFailedToSuccessEventId(GOP_PAYMENT_FAILED_TO_SUCCESS)
                .updateDraftEventId(GOP_UPDATE_DRAFT)
                .build())
            .put(STANDING_SEARCH, CaseEvents.builder()
                .createCaseApplicationEventId(GOP_CREATE_APPLICATION)
                .createCaseEventId(GOP_CREATE_CASE)
                .createDraftEventId(GOP_CREATE_DRAFT)
                .paymentFailedAgainEventId(GOP_PAYMENT_FAILED_AGAIN)
                .paymentFailedEventId(GOP_PAYMENT_FAILED)
                .paymentFailedToSuccessEventId(GOP_PAYMENT_FAILED_TO_SUCCESS)
                .updateDraftEventId(GOP_UPDATE_DRAFT)
                .build())
            .build();
    }

    @Bean
    public Map<CaseType, String> searchFieldsMap() {
        return ImmutableMap.<CaseType, String>builder()
            .put(GRANT_OF_REPRESENTATION, "reference")
            .put(CAVEAT, "applicationId")
            .put(WILL_LODGEMENT, "deceasedEmailAddress")
            .put(STANDING_SEARCH, "applicantEmailAddress")
            .build();
    }

    @Bean
    public Map<CaseType, CaseState> createdStateMap() {
        return ImmutableMap.<CaseType, CaseState>builder()
            .put(GRANT_OF_REPRESENTATION, CaseState.PA_APP_CREATED)
            .put(CAVEAT, CaseState.PA_APP_CREATED)
            .build();
    }
}
