package uk.gov.hmcts.probate.services.submit.core;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.probate.services.submit.services.FeatureToggleService;

import java.time.Duration;

@Slf4j
@Service
public class FeatureToggleServiceImpl implements FeatureToggleService {
    private final LDClientInterface ldClient;
    private final LDContext ldContext;

    @Autowired
    public FeatureToggleServiceImpl(
            final LDClientInterface ldClient,
            @Value("${ld.user.key}") final String ldUserKey,
            @Value("${ld.user.firstName}") final String ldUserFirstName,
            @Value("${ld.user.lastName}") final String ldUserLastName) {

        final String contextName = new StringBuilder()
                .append(ldUserFirstName)
                .append(" ")
                .append(ldUserLastName)
                .toString();

        this.ldClient = ldClient;
        this.ldContext = LDContext.builder(ldUserKey + "_subm_service")
                .name(contextName)
                .kind("application")
                .set("timestamp", String.valueOf(System.currentTimeMillis()))
                .build();
    }

    private boolean isFeatureToggleOn(
            final String featureToggleCode,
            final boolean defaultValue) {
        return this.ldClient.boolVariation(featureToggleCode, this.ldContext, defaultValue);
    }

    @Override
    public boolean causeLookupTimeout() {
        return isFeatureToggleOn("probate-enable-submit-lookup-timeout", false);
    }

    @Override
    public boolean causeLookupFailure() {
        return isFeatureToggleOn("probate-enable-submit-lookup-failure", false);
    }

    @Override
    public void doSleep() {
        try {
            Thread.sleep(Duration.ofSeconds(60));
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for submit lookup timeout", e);
            Thread.currentThread().interrupt();
            throw new FeatureToggleException(e);
        }
    }

    @Override
    public void throwEx() {
        throw new FeatureToggleException();
    }

    static final class FeatureToggleException extends RuntimeException {
        FeatureToggleException() {
            super();
        }

        FeatureToggleException(final Exception e) {
            super(e);
        }
    }
}
