package uk.gov.hmcts.probate.services.submit.core;

import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SearchFieldFactory {

    private static final String EXECUTORS_APPLYING_VALUE_APPLYING_EXECUTOR_INVITIATION_ID =
        "data.executorsApplying.value.applyingExecutorInvitationId";
    private final Map<CaseType, String> searchFieldsMap;

    public Pair<String, String> getSearchFieldValuePair(CaseType caseType, CaseData caseData) {
        Assert.isTrue(CaseType.getCaseType(caseData).equals(caseType), "CaseType is not correct for CaseData subtype");
        String caseField = getSearchFieldName(caseType);
        try {
            String value = (String) PropertyUtils.getProperty(caseData, caseField);
            return ImmutablePair.of(caseField, value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Cannot find property value for field " + caseField);
        }
    }

    public String getSearchFieldName(CaseType caseType) {
        return Optional.ofNullable(searchFieldsMap.get(caseType)).orElseThrow(IllegalArgumentException::new);
    }

    public String getEsSearchFieldName(CaseType caseType) {
        if (caseType.equals(CaseType.GRANT_OF_REPRESENTATION)) {
            return Optional.ofNullable(searchFieldsMap.get(caseType)).orElseThrow(IllegalArgumentException::new);
        } else {
            return Optional.ofNullable("data." + searchFieldsMap.get(caseType))
                .orElseThrow(IllegalArgumentException::new);
        }
    }

    public String getSearchInviteFieldName() {
        return EXECUTORS_APPLYING_VALUE_APPLYING_EXECUTOR_INVITIATION_ID;
    }

    public String getSearchApplicantEmailFieldName() {
        return "data.primaryApplicantEmailAddress";
    }
}
