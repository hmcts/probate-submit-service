package uk.gov.hmcts.probate.services.submit.core;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.probate.security.SecurityDto;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.clients.v2.ccd.CaseResponseBuilder;
import uk.gov.hmcts.probate.services.submit.clients.v2.ccd.CcdElasticSearchQueryBuilder;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.CAVEAT_AWAITING_RESOLUTION;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.CAVEAT_AWAITING_WARNING_RESPONSE;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.CAVEAT_NOT_MATCHED;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.CAVEAT_RAISED;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.CAVEAT_WARNING_VALIDATION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.CAVEAT_APPLY_FOR_AWAITING_WARNING_RESPONSE;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.CAVEAT_APPLY_FOR_WARNNG_VALIDATION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.CAVEAT_EXPIRED_FOR_AWAITING_RESOLUTION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.CAVEAT_EXPIRED_FOR_CAVEAT_NOT_MATCHED;

@ExtendWith(SpringExtension.class)
public class CaveatExpiryServiceImplTest {
    private static final String EXPIRY_DATE = "2020-12-31";
    private static final String SEARCH_QUERY = "Search query";
    private static final String CAVEAT_ID = "1234567890";
    private static final LocalDateTime LAST_MODIFIED_DATE_TIME = LocalDateTime.of(2019, 1, 1, 0, 0, 0);
    private static final String EVENT_DESCRIPTOR_CAVEAT_EXPIRED = "Caveat Auto Expired";
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseResponseBuilder caseResponseBuilder;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private CcdElasticSearchQueryBuilder elasticSearchQueryBuilder;
    @InjectMocks
    private CaveatExpiryServiceImpl caveatExpiryService;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void shouldExpireNoCaveatsWhenNoneHaveExpired() {
        SecurityDto securityDto = SecurityDto.builder().build();
        SearchResult searchResult = SearchResult.builder().cases(Collections.emptyList()).build();

        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(elasticSearchQueryBuilder.buildQueryForCaveatExpiry(EXPIRY_DATE)).thenReturn(SEARCH_QUERY);
        when(coreCaseDataApi.searchCases(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            CaseType.CAVEAT.getName(),
            SEARCH_QUERY)).thenReturn(searchResult);

        List<ProbateCaseDetails> expiredCaveats = caveatExpiryService.expireCaveats(EXPIRY_DATE);

        assertNotNull(expiredCaveats);
        assertEquals(0, expiredCaveats.size());
        verify(securityUtils, times(1)).getSecurityDto();
        verify(elasticSearchQueryBuilder, times(1)).buildQueryForCaveatExpiry(EXPIRY_DATE);
    }

    @Test
    public void shouldExpireCaveatsNotMatched() {
        CaseState caseState = CAVEAT_NOT_MATCHED;
        final EventId eventId = CAVEAT_EXPIRED_FOR_CAVEAT_NOT_MATCHED;
        CaseInfo caseInfo = CaseInfo.builder().state(caseState)
            .caseId(CAVEAT_ID)
            .lastModifiedDateTime(LAST_MODIFIED_DATE_TIME)
            .build();
        CaveatData caseData = CaveatData.builder().build();
        SecurityDto securityDto = SecurityDto.builder().build();
        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseInfo(caseInfo)
            .caseData(caseData)
            .build();
        CaseDetails caseDetails = CaseDetails.builder().state(caseState.name())
            .build();
        SearchResult searchResult = SearchResult.builder().cases(Lists.newArrayList(caseDetails)).build();

        when(caseResponseBuilder.createCaseResponse(caseDetails)).thenReturn(probateCaseDetails);
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(elasticSearchQueryBuilder.buildQueryForCaveatExpiry(EXPIRY_DATE)).thenReturn(SEARCH_QUERY);
        when(coreCaseDataApi.searchCases(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            CaseType.CAVEAT.getName(),
            SEARCH_QUERY)).thenReturn(searchResult);

        List<ProbateCaseDetails> expiredCaveats = caveatExpiryService.expireCaveats(EXPIRY_DATE);

        assertNotNull(expiredCaveats);
        assertEquals(1, expiredCaveats.size());
        assertThat(expiredCaveats.get(0).getCaseData().getClass().getSimpleName()).contains("CaveatData");
        assertEquals(true, ((CaveatData) expiredCaveats.get(0).getCaseData()).getAutoClosedExpiry());
        verify(securityUtils, times(1)).getSecurityDto();
        verify(elasticSearchQueryBuilder, times(1)).buildQueryForCaveatExpiry(EXPIRY_DATE);
        verify(coreCaseDataService, times(1)).updateCaseAsCaseworker(probateCaseDetails.getCaseInfo().getCaseId(),
                probateCaseDetails.getCaseInfo().getLastModifiedDateTime(), probateCaseDetails.getCaseData(),
                eventId, securityDto, EVENT_DESCRIPTOR_CAVEAT_EXPIRED);
    }

    @Test
    public void shouldExpireCaveatsAwaitingResolution() {
        CaseState caseState = CAVEAT_AWAITING_RESOLUTION;
        final EventId eventId = CAVEAT_EXPIRED_FOR_AWAITING_RESOLUTION;
        CaseInfo caseInfo = CaseInfo.builder().state(caseState)
            .caseId(CAVEAT_ID)
            .build();
        CaveatData caseData = CaveatData.builder().build();
        SecurityDto securityDto = SecurityDto.builder().build();
        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseInfo(caseInfo)
            .caseData(caseData)
            .build();
        CaseDetails caseDetails = CaseDetails.builder().state(caseState.name())
            .build();
        SearchResult searchResult = SearchResult.builder().cases(Lists.newArrayList(caseDetails)).build();

        when(caseResponseBuilder.createCaseResponse(caseDetails)).thenReturn(probateCaseDetails);
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(elasticSearchQueryBuilder.buildQueryForCaveatExpiry(EXPIRY_DATE)).thenReturn(SEARCH_QUERY);
        when(coreCaseDataApi.searchCases(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            CaseType.CAVEAT.getName(),
            SEARCH_QUERY)).thenReturn(searchResult);

        List<ProbateCaseDetails> expiredCaveats = caveatExpiryService.expireCaveats(EXPIRY_DATE);

        assertNotNull(expiredCaveats);
        assertEquals(1, expiredCaveats.size());
        assertThat(expiredCaveats.get(0).getCaseData().getClass().getSimpleName()).contains("CaveatData");
        assertEquals(true, ((CaveatData) expiredCaveats.get(0).getCaseData()).getAutoClosedExpiry());

        verify(securityUtils, times(1)).getSecurityDto();
        verify(elasticSearchQueryBuilder, times(1)).buildQueryForCaveatExpiry(EXPIRY_DATE);
        verify(coreCaseDataService, times(1)).updateCaseAsCaseworker(probateCaseDetails.getCaseInfo().getCaseId(),
                probateCaseDetails.getCaseInfo().getLastModifiedDateTime(), probateCaseDetails.getCaseData(),
                eventId, securityDto, EVENT_DESCRIPTOR_CAVEAT_EXPIRED);
    }

    @Test
    public void shouldExpireCaveatsAwaitingWarningResponse() {
        CaseState caseState = CAVEAT_AWAITING_WARNING_RESPONSE;
        final EventId eventId = CAVEAT_APPLY_FOR_AWAITING_WARNING_RESPONSE;
        CaseInfo caseInfo = CaseInfo.builder().state(caseState)
            .caseId(CAVEAT_ID)
            .build();
        CaveatData caseData = CaveatData.builder().build();
        SecurityDto securityDto = SecurityDto.builder().build();
        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseInfo(caseInfo)
            .caseData(caseData)
            .build();
        CaseDetails caseDetails = CaseDetails.builder().state(caseState.name())
            .build();
        SearchResult searchResult = SearchResult.builder().cases(Lists.newArrayList(caseDetails)).build();

        when(caseResponseBuilder.createCaseResponse(caseDetails)).thenReturn(probateCaseDetails);
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(elasticSearchQueryBuilder.buildQueryForCaveatExpiry(EXPIRY_DATE)).thenReturn(SEARCH_QUERY);
        when(coreCaseDataApi.searchCases(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            CaseType.CAVEAT.getName(),
            SEARCH_QUERY)).thenReturn(searchResult);

        List<ProbateCaseDetails> expiredCaveats = caveatExpiryService.expireCaveats(EXPIRY_DATE);

        assertNotNull(expiredCaveats);
        assertEquals(1, expiredCaveats.size());
        assertThat(expiredCaveats.get(0).getCaseData().getClass().getSimpleName()).contains("CaveatData");
        assertEquals(true, ((CaveatData) expiredCaveats.get(0).getCaseData()).getAutoClosedExpiry());
        verify(securityUtils, times(1)).getSecurityDto();
        verify(elasticSearchQueryBuilder, times(1)).buildQueryForCaveatExpiry(EXPIRY_DATE);
        verify(coreCaseDataService, times(1)).updateCaseAsCaseworker(probateCaseDetails.getCaseInfo().getCaseId(),
                probateCaseDetails.getCaseInfo().getLastModifiedDateTime(), probateCaseDetails.getCaseData(),
                eventId, securityDto, EVENT_DESCRIPTOR_CAVEAT_EXPIRED);
    }

    @Test
    public void shouldExpireCaveatsWarningValidation() {
        CaseState caseState = CAVEAT_WARNING_VALIDATION;
        final EventId eventId = CAVEAT_APPLY_FOR_WARNNG_VALIDATION;
        CaseInfo caseInfo = CaseInfo.builder().state(caseState)
            .caseId(CAVEAT_ID)
            .build();
        CaveatData caseData = CaveatData.builder().build();
        SecurityDto securityDto = SecurityDto.builder().build();
        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseInfo(caseInfo)
            .caseData(caseData)
            .build();
        CaseDetails caseDetails = CaseDetails.builder().state(caseState.name())
            .build();
        SearchResult searchResult = SearchResult.builder().cases(Lists.newArrayList(caseDetails)).build();

        when(caseResponseBuilder.createCaseResponse(caseDetails)).thenReturn(probateCaseDetails);
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(elasticSearchQueryBuilder.buildQueryForCaveatExpiry(EXPIRY_DATE)).thenReturn(SEARCH_QUERY);
        when(coreCaseDataApi.searchCases(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            CaseType.CAVEAT.getName(),
            SEARCH_QUERY)).thenReturn(searchResult);

        List<ProbateCaseDetails> expiredCaveats = caveatExpiryService.expireCaveats(EXPIRY_DATE);

        assertNotNull(expiredCaveats);
        assertEquals(1, expiredCaveats.size());
        assertThat(expiredCaveats.get(0).getCaseData().getClass().getSimpleName()).contains("CaveatData");
        assertEquals(true, ((CaveatData) expiredCaveats.get(0).getCaseData()).getAutoClosedExpiry());
        verify(securityUtils, times(1)).getSecurityDto();
        verify(elasticSearchQueryBuilder, times(1)).buildQueryForCaveatExpiry(EXPIRY_DATE);
        verify(coreCaseDataService, times(1)).updateCaseAsCaseworker(probateCaseDetails.getCaseInfo().getCaseId(),
                probateCaseDetails.getCaseInfo().getLastModifiedDateTime(), probateCaseDetails.getCaseData(),
                eventId, securityDto, EVENT_DESCRIPTOR_CAVEAT_EXPIRED);
    }

    @Test
    public void shouldFailExpireCaveatsWarningValidation() {
        CaseState caseState = CAVEAT_WARNING_VALIDATION;
        final EventId eventId = CAVEAT_APPLY_FOR_WARNNG_VALIDATION;

        CaseInfo caseInfo = CaseInfo.builder().state(caseState)
            .caseId(CAVEAT_ID)
            .build();
        CaveatData caseData = CaveatData.builder().build();
        SecurityDto securityDto = SecurityDto.builder().build();
        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseInfo(caseInfo)
            .caseData(caseData)
            .build();
        CaseDetails caseDetails = CaseDetails.builder().state(caseState.name())
            .build();
        SearchResult searchResult = SearchResult.builder().cases(Lists.newArrayList(caseDetails)).build();

        when(caseResponseBuilder.createCaseResponse(caseDetails)).thenReturn(probateCaseDetails);
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(elasticSearchQueryBuilder.buildQueryForCaveatExpiry(EXPIRY_DATE)).thenReturn(SEARCH_QUERY);
        when(coreCaseDataApi.searchCases(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            CaseType.CAVEAT.getName(),
            SEARCH_QUERY)).thenReturn(searchResult);
        RuntimeException e = new RuntimeException("Problem updating caveat");
        when(coreCaseDataService.updateCaseAsCaseworker(probateCaseDetails.getCaseInfo().getCaseId(),
                probateCaseDetails.getCaseInfo().getLastModifiedDateTime(), probateCaseDetails.getCaseData(),
                eventId, securityDto, EVENT_DESCRIPTOR_CAVEAT_EXPIRED)).thenThrow(e);

        List<ProbateCaseDetails> expiredCaveats = caveatExpiryService.expireCaveats(EXPIRY_DATE);

        assertNotNull(expiredCaveats);
        assertEquals(1, expiredCaveats.size());
        assertThat(expiredCaveats.get(0).getCaseData().getClass().getSimpleName()).contains("CaveatData");
        assertEquals(true, ((CaveatData) expiredCaveats.get(0).getCaseData()).getAutoClosedExpiry());
        verify(securityUtils, times(1)).getSecurityDto();
        verify(elasticSearchQueryBuilder, times(1)).buildQueryForCaveatExpiry(EXPIRY_DATE);
    }

    @Test
    public void shouldThrowExceptionWhenExpiringCaveatInInvalidState() {
        CaseState caseState = CAVEAT_RAISED;

        CaseInfo caseInfo = CaseInfo.builder().state(caseState)
            .caseId(CAVEAT_ID)
            .build();
        CaveatData caseData = CaveatData.builder().build();
        SecurityDto securityDto = SecurityDto.builder().build();
        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseInfo(caseInfo)
            .caseData(caseData)
            .build();
        CaseDetails caseDetails = CaseDetails.builder().state(caseState.name())
            .build();
        SearchResult searchResult = SearchResult.builder().cases(Lists.newArrayList(caseDetails)).build();

        when(caseResponseBuilder.createCaseResponse(caseDetails)).thenReturn(probateCaseDetails);
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(elasticSearchQueryBuilder.buildQueryForCaveatExpiry(EXPIRY_DATE)).thenReturn(SEARCH_QUERY);
        when(coreCaseDataApi.searchCases(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            CaseType.CAVEAT.getName(),
            SEARCH_QUERY)).thenReturn(searchResult);

        assertThrows(IllegalStateException.class, () -> {
            caveatExpiryService.expireCaveats(EXPIRY_DATE);
        });
    }
}
