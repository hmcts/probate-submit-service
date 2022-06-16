package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class CcdElasticSearchQueryBuilderTest {


    CcdElasticSearchQueryBuilder ccdElasticSearchQueryBuilder = new CcdElasticSearchQueryBuilder();

    @Test
    public void shouldBuildQuery() {
        String result = ccdElasticSearchQueryBuilder
            .buildQuery("123456", "data.executorsApplying.value.applyingExecutorInvitationId");
        assertThat(result, Matchers.equalTo("{\"query\":{\"term\":"
            + "{ \"data.executorsApplying.value.applyingExecutorInvitationId.keyword\":\"123456\"}}}"));

    }

    @Test
    public void shouldBuildAllCasesQuery() {
        String result = ccdElasticSearchQueryBuilder.buildFindAllCasesQuery();
        assertThat(result, Matchers.equalTo("{\"query\":{\"match_all\":{}},\"size\": 50}"));

    }

    @Test
    public void shouldBuildCaveatExpiryQuery() {
        String result = ccdElasticSearchQueryBuilder.buildQueryForCaveatExpiry("2020-12-31");
        assertThat(result,
            Matchers.equalTo("{\"query\":{\"bool\":{\"must\":[{\"match\":{\"data.expiryDate\":\"2020-12-31\"}}]"
                + ",\"should\":[{\"match\":{\"state\":\"CaveatNotMatched\"}},{\"match\":"
                + "{\"state\":\"AwaitingCaveatResolution\"}}"
                + ",{\"match\":{\"state\":\"WarningValidation\"}},"
                + "{\"match\":{\"state\":\"AwaitingWarningResponse\"}}],\"minimum_should_match\":1}},\"size\": 100}"));

    }
}
