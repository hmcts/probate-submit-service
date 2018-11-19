package uk.gov.hmcts.probate.services.submit.model.v2.grantofrepresentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LegalStatementExecutorApplying {

    private final String name;

    @JsonProperty(value = "sign")
    private final String sign;

}
