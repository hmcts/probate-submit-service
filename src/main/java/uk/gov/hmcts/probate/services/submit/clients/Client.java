package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Calendar;

@FunctionalInterface
interface Client<T, R> {
    R execute(T t, JsonNode sequenceNumber, Calendar submissionTimestamp);
}
