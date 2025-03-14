package uk.gov.hmcts.probate.services.submit.services;

public interface FeatureToggleService {
    boolean causeLookupTimeout();

    boolean causeLookupFailure();

    void doSleep();

    void throwEx();
}
