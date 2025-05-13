package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NoArgsConstructor
public class CcdElasticSearchQueryBuilder {

    String buildQuery(String searchValue, String searchField) {
        String searchString = "{\"query\":{\"term\":{ \""
            + searchField
            + ".keyword\":\"" + searchValue + "\"}}}";
        return searchString;
    }
}
