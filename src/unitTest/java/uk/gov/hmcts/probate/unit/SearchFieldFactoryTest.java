package uk.gov.hmcts.probate.unit;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.probate.services.submit.core.SearchFieldFactory;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.CAVEAT;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.GRANT_OF_REPRESENTATION;

public class SearchFieldFactoryTest {

    private static final String IDENTIFIER = "234324";
    private SearchFieldFactory searchFieldFactory;

    @Before
    public void setUp(){
        Map<CaseType, String> searchFieldMap = ImmutableMap.<CaseType, String>builder()
                .put(GRANT_OF_REPRESENTATION, "primaryApplicantEmailAddress")
                .put(CAVEAT, "applicationId")
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

    @Test
    public void shouldGetSearchFieldValuePairForCaveats(){
        CaveatData caveatData = new CaveatData();
        caveatData.setApplicationId(IDENTIFIER);
        Pair<String, String> searchFieldValuePair =
                searchFieldFactory.getSearchFieldValuePair(CaseType.CAVEAT, caveatData);
        assertThat(searchFieldValuePair.getLeft(), equalTo("applicationId"));
        assertThat(searchFieldValuePair.getRight(), equalTo(IDENTIFIER));
    }
    @Test
    public void shouldGetESSearchFieldValues(){
        assertThat(searchFieldFactory.getEsSearchFieldName(CaseType.CAVEAT), equalTo("data.applicationId"));
        assertThat(searchFieldFactory.getEsSearchFieldName(CaseType.GRANT_OF_REPRESENTATION), equalTo("primaryApplicantEmailAddress"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionForMismatchingTypeAndData(){
        CaveatData caveatData = new CaveatData();
        caveatData.setCaveatorEmailAddress(IDENTIFIER);

        searchFieldFactory.getSearchFieldValuePair(GRANT_OF_REPRESENTATION, caveatData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenFieldDoesNotExist(){
        Map<CaseType, String> searchFieldMap = ImmutableMap.<CaseType, String>builder()
            .put(GRANT_OF_REPRESENTATION, "random")
            .build();
        SearchFieldFactory searchFieldFactory = new SearchFieldFactory(searchFieldMap);

        GrantOfRepresentationData grantOfRepresentationData = new GrantOfRepresentationData();

        searchFieldFactory.getSearchFieldValuePair(GRANT_OF_REPRESENTATION, grantOfRepresentationData);
    }

    @Test
    public void shouldGetInviteFieldName(){
        assertThat(searchFieldFactory.getSearchInviteFieldName(), equalTo("data.executorsApplying.value.applyingExecutorInvitationId"));
    }

    @Test
    public void shouldGetApplicantEmailFieldName(){
        assertThat(searchFieldFactory.getSearchApplicantEmailFieldName(), equalTo("data.primaryApplicantEmailAddress"));
    }
}
