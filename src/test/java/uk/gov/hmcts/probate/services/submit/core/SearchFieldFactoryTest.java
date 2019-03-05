package uk.gov.hmcts.probate.services.submit.core;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.GRANT_OF_REPRESENTATION;

public class SearchFieldFactoryTest {

    private static final String IDENTIFIER = "234324";
    private SearchFieldFactory searchFieldFactory;

    @Before
    public void setUp(){
        Map<CaseType, String> searchFieldMap = ImmutableMap.<CaseType, String>builder()
                .put(GRANT_OF_REPRESENTATION, "primaryApplicantEmailAddress")
                .build();
        searchFieldFactory = new SearchFieldFactory(searchFieldMap);
    }

    @Test
    public void shouldGetSearchFieldValuePairForGrantOfRepresentation(){
        GrantOfRepresentationData grantOfRepresentationData = new GrantOfRepresentationData();
        grantOfRepresentationData.setPrimaryApplicantEmailAddress(IDENTIFIER);

        Pair<String, String> searchFieldValuePair =
                searchFieldFactory.getSearchFieldValuePair(CaseType.GRANT_OF_REPRESENTATION, grantOfRepresentationData);

        assertThat(searchFieldValuePair.getLeft(), equalTo("primaryApplicantEmailAddress"));
        assertThat(searchFieldValuePair.getRight(), equalTo(IDENTIFIER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenConfigDoesNotExistForType(){
        CaveatData caveatData = new CaveatData();
        caveatData.setCaveatorEmailAddress(IDENTIFIER);

        searchFieldFactory.getSearchFieldValuePair(CaseType.CAVEAT, caveatData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionForMismatchingTypeAndData(){
        CaveatData caveatData = new CaveatData();
        caveatData.setCaveatorEmailAddress(IDENTIFIER);

        searchFieldFactory.getSearchFieldValuePair(GRANT_OF_REPRESENTATION, caveatData);
    }
}
