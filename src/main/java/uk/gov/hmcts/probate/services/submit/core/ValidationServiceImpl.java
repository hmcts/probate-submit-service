package uk.gov.hmcts.probate.services.submit.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseValidationException;
import uk.gov.hmcts.probate.services.submit.services.ValidationService;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantType;
import uk.gov.hmcts.reform.probate.model.validation.groups.crossfieldcheck.IntestacyCrossFieldCheck;
import uk.gov.hmcts.reform.probate.model.validation.groups.crossfieldcheck.PaCrossFieldCheck;
import uk.gov.hmcts.reform.probate.model.validation.groups.fieldcheck.IntestacyFieldCheck;
import uk.gov.hmcts.reform.probate.model.validation.groups.fieldcheck.PaFieldCheck;
import uk.gov.hmcts.reform.probate.model.validation.groups.nullcheck.IntestacyNullCheck;
import uk.gov.hmcts.reform.probate.model.validation.groups.nullcheck.PaNullCheck;
import uk.gov.hmcts.reform.probate.model.validation.groups.submission.IntestacySubmission;
import uk.gov.hmcts.reform.probate.model.validation.groups.submission.PaSubmission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationServiceImpl implements ValidationService {

    private static final List<Class> PA_VALIDATION_GROUPS =
        Lists.newArrayList(PaNullCheck.class, PaFieldCheck.class, PaCrossFieldCheck.class);

    private static final List<Class> INTESTACY_VALIDATION_GROUPS =
        Lists.newArrayList(IntestacyNullCheck.class, IntestacyFieldCheck.class, IntestacyCrossFieldCheck.class);

    private static final List<Class> CAVEAT_VALIDATION_GROUPS = Lists.newArrayList(Default.class);

    private static final List<Class> PA_SUBMISSION_GROUPS = Lists.newArrayList(PaSubmission.class);

    private static final List<Class> INTESTACY_SUBMISSION_GROUPS = Lists.newArrayList(IntestacySubmission.class);

    private static final List<Class> CAVEAT_SUBMISSION_GROUPS = Lists.newArrayList();

    private final Map<GrantType, List<Class>> grantTypeValidationGroupMap =
        ImmutableMap.<GrantType, List<Class>>builder()
            .put(GrantType.GRANT_OF_PROBATE, PA_VALIDATION_GROUPS)
            .put(GrantType.INTESTACY, INTESTACY_VALIDATION_GROUPS)
            .build();


    private final Map<CaseType, Function<CaseData, List<Class>>> caseTypeValidationGroupMap =
        ImmutableMap.<CaseType, Function<CaseData, List<Class>>>builder()
            .put(CaseType.GRANT_OF_REPRESENTATION, this::getGroupClassesForGrantOfRepresentation)
            .put(CaseType.CAVEAT, caseData -> CAVEAT_VALIDATION_GROUPS)
            .build();

    private final Map<GrantType, List<Class>> grantTypeSubmissionGroupMap =
        ImmutableMap.<GrantType, List<Class>>builder()
            .put(GrantType.GRANT_OF_PROBATE, PA_SUBMISSION_GROUPS)
            .put(GrantType.INTESTACY, INTESTACY_SUBMISSION_GROUPS)
            .build();


    private final Map<CaseType, Function<CaseData, List<Class>>> caseTypeSubmissionGroupMap =
        ImmutableMap.<CaseType, Function<CaseData, List<Class>>>builder()
            .put(CaseType.GRANT_OF_REPRESENTATION, this::getGroupSubmissionClassesForGrantOfRepresentation)
            .put(CaseType.CAVEAT, caseData -> CAVEAT_SUBMISSION_GROUPS)
            .build();


    private final Validator validator;

    @Override
    public void validate(ProbateCaseDetails probateCaseDetails) {
        validate(probateCaseDetails, Lists.newArrayList());
    }

    private void validate(ProbateCaseDetails probateCaseDetails, List<Class> submissionGroups) {
        log.info("validationServiceImpl.validate caseId: {}", (probateCaseDetails.getCaseInfo() != null
            ? probateCaseDetails.getCaseInfo().getCaseId() : "NA"));
        CaseData caseData = probateCaseDetails.getCaseData();
        CaseType caseType = CaseType.getCaseType(caseData);
        List<Class> validationGroupClasses = caseTypeValidationGroupMap.get(caseType).apply(caseData);
        List<Class> allValidationGroups = new ArrayList<>();
        allValidationGroups.addAll(validationGroupClasses);
        allValidationGroups.addAll(submissionGroups);
        Class[] validationClassesArray = allValidationGroups.toArray(new Class[allValidationGroups.size()]);
        Set<ConstraintViolation<CaseData>> constraintViolations = validator.validate(caseData, validationClassesArray);
        if (!constraintViolations.isEmpty()) {
            throw new CaseValidationException(constraintViolations);
        }
    }

    @Override
    public void validateForSubmission(ProbateCaseDetails probateCaseDetails) {
        CaseData caseData = probateCaseDetails.getCaseData();
        CaseType caseType = CaseType.getCaseType(caseData);
        List<Class> submissionGroupClasses = caseTypeSubmissionGroupMap.get(caseType).apply(caseData);
        validate(probateCaseDetails, submissionGroupClasses);
    }

    private List<Class> getGroupClassesForGrantOfRepresentation(CaseData caseData) {
        GrantOfRepresentationData grantOfRepresentationData = (GrantOfRepresentationData) caseData;
        return grantTypeValidationGroupMap.get(grantOfRepresentationData.getGrantType());
    }

    private List<Class> getGroupSubmissionClassesForGrantOfRepresentation(CaseData caseData) {
        GrantOfRepresentationData grantOfRepresentationData = (GrantOfRepresentationData) caseData;
        return grantTypeSubmissionGroupMap.get(grantOfRepresentationData.getGrantType());
    }
}
