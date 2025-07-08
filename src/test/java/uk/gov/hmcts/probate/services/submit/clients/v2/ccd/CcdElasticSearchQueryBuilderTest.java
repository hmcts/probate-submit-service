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
}
