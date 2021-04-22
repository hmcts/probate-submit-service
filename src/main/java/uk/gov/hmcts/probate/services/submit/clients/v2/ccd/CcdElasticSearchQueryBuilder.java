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

    public String buildQueryForCaveatExpiry(String expiryDate) {
        String searchString =
            "{\"query\":{\"bool\":{\"must\":[{\"match\":{\"data.expiryDate\":\"" + expiryDate + "\"}}],"
                + "\"should\":[{\"match\":{\"state\":\"CaveatNotMatched\"}},"
                + "{\"match\":{\"state\":\"AwaitingCaveatResolution\"}},"
                + "{\"match\":{\"state\":\"WarningValidation\"}},"
                + "{\"match\":{\"state\":\"AwaitingWarningResponse\"}}],\"minimum_should_match\":1}},\"size\": 100}";
        return searchString;
    }

    String buildFindAllCasesQuery() {
        return "{\"query\":{\"match_all\":{}},\"size\": 50}";
    }


}
