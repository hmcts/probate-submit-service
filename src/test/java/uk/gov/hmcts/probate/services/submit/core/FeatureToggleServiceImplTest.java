package uk.gov.hmcts.probate.services.submit.core;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertFalse;

class FeatureToggleServiceImplTest {
    LDClientInterface ldClientMock;
    FeatureToggleServiceImpl featureToggleService;

    @BeforeEach
    void setUp() {
        ldClientMock = mock(LDClient.class);

        when(ldClientMock.boolVariation(any(), any(LDContext.class), anyBoolean()))
                .thenReturn(false);
        featureToggleService = new FeatureToggleServiceImpl(ldClientMock, "", "", "");
    }

    @Test
    void causeLookupTimeout() {
        final boolean actual = featureToggleService.causeLookupTimeout();

        assertFalse(actual);
    }

    @Test
    void causeLookupFailure() {
        final boolean actual = featureToggleService.causeLookupFailure();

        assertFalse(actual);
    }

    @Test
    void testSleepNoInterrupt() {
        assertDoesNotThrow(() -> featureToggleService.doSleep());
    }

    @Test
    void testThrowEx() {
        assertThrows(FeatureToggleServiceImpl.FeatureToggleException.class, () -> featureToggleService.throwEx());
    }
}