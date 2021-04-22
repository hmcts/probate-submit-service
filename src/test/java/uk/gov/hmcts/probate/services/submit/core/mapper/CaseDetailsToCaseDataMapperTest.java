package uk.gov.hmcts.probate.services.submit.core.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.probate.services.submit.clients.v2.ccd.CaseDetailsToCaseDataMapper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;

public class CaseDetailsToCaseDataMapperTest {

    private CaseDetailsToCaseDataMapper caseDetailsToCaseDataMapper;

    @Before
    public void setUp() {
        caseDetailsToCaseDataMapper = new CaseDetailsToCaseDataMapper(new ObjectMapper());
    }

    @Test
    public void shouldMap() {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
            .put("applicationType", "Personal")
            .put("caseType", "intestacy")
            .put("deceasedForenames", "Robert")
            .put("deceasedSurname", "Baratheon")
            .build();
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId("GrantOfRepresentation").data(map).build();

        CaseData caseData = caseDetailsToCaseDataMapper.map(caseDetails);

        assertThat(caseData, Matchers.instanceOf(GrantOfRepresentationData.class));
    }

    @Test
    public void shouldThrowMappingException() {
        Map<String, Object> map = new HashMap();
        map.put("applicationType", "Personal");
        map.put("caseType", "intestacy");
        map.put("deceasedForenames", "Robert");
        map.put("deceasedSurname", null);

        CaseDetails caseDetails = CaseDetails.builder().caseTypeId("GrantOfRepresentation").data(map).build();

        CaseData caseData = caseDetailsToCaseDataMapper.map(caseDetails);
        assertThat(caseData, Matchers.instanceOf(GrantOfRepresentationData.class));
    }
}