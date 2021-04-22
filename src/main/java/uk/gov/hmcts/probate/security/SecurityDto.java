package uk.gov.hmcts.probate.security;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SecurityDto {

    private String authorisation;

    private String userId;

    private String serviceAuthorisation;
}
