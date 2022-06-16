package uk.gov.hmcts.probate.services.submit.core;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.CAVEAT;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.GRANT_OF_REPRESENTATION;

public class SearchFieldFactoryTest {

    private static final String IDENTIFIER = "234324";
    private SearchFieldFactory searchFieldFactory;

    @BeforeEach
    public void setUp() {
        Map<CaseType, String> searchFieldMap = ImmutableMap.<CaseType, String>builder()
            .put(GRANT_OF_REPRESENTATION, "primaryApplicantEmailAddress")
            .put(CAVEAT, "applicationId")
            .build();
        searchFieldFactory = new SearchFieldFactory(searchFieldMap);
    }

    @Test
    public void shouldGetSearchFieldValuePairForGrantOfRepresentation() {
        GrantOfRepresentationData grantOfRepresentationData = new GrantOfRepresentationData();
        grantOfRepresentationData.setPrimaryApplicantEmailAddress(IDENTIFIER);
        Pair<String, String> searchFieldValuePair =
            searchFieldFactory.getSearchFieldValuePair(CaseType.GRANT_OF_REPRESENTATION, grantOfRepresentationData);
        assertEquals("primaryApplicantEmailAddress", searchFieldValuePair.getLeft());
        assertEquals(IDENTIFIER, searchFieldValuePair.getRight());
    }

    @Test
    public void shouldGetSearchFieldValuePairForCaveats() {
        CaveatData caveatData = new CaveatData();
        caveatData.setApplicationId(IDENTIFIER);
        Pair<String, String> searchFieldValuePair =
            searchFieldFactory.getSearchFieldValuePair(CaseType.CAVEAT, caveatData);
        assertEquals("applicationId", searchFieldValuePair.getLeft());
        assertEquals(IDENTIFIER, searchFieldValuePair.getRight());
    }

    @Test
    public void shouldGetEsSearchFieldValues() {
        assertEquals("data.applicationId", searchFieldFactory.getEsSearchFieldName(CaseType.CAVEAT));
        assertEquals("primaryApplicantEmailAddress",
                searchFieldFactory.getEsSearchFieldName(CaseType.GRANT_OF_REPRESENTATION));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionForMismatchingTypeAndData() {
        CaveatData caveatData = new CaveatData();
        caveatData.setCaveatorEmailAddress(IDENTIFIER);

        assertThrows(IllegalArgumentException.class, () -> {
            searchFieldFactory.getSearchFieldValuePair(GRANT_OF_REPRESENTATION, caveatData);
        });
    }

    @Test
    public void shouldThrowExceptionWhenFieldDoesNotExist() {
        Map<CaseType, String> searchFieldMap = ImmutableMap.<CaseType, String>builder()
            .put(GRANT_OF_REPRESENTATION, "random")
            .build();
        SearchFieldFactory searchFieldFactory = new SearchFieldFactory(searchFieldMap);

        GrantOfRepresentationData grantOfRepresentationData = new GrantOfRepresentationData();

        assertThrows(IllegalArgumentException.class, () -> {
            searchFieldFactory.getSearchFieldValuePair(GRANT_OF_REPRESENTATION, grantOfRepresentationData);
        });
    }

    @Test
    public void shouldGetInviteFieldName() {
        assertEquals("data.executorsApplying.value.applyingExecutorInvitationId",
                searchFieldFactory.getSearchInviteFieldName());
    }

    @Test
    public void shouldGetApplicantEmailFieldName() {
        assertEquals("data.primaryApplicantEmailAddress", searchFieldFactory.getSearchApplicantEmailFieldName());
    }
}
