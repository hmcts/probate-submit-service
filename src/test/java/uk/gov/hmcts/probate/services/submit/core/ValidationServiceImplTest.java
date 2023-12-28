package uk.gov.hmcts.probate.services.submit.core;

import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseValidationException;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;
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

import java.util.HashSet;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValidationServiceImplTest {

    private Class[] paValidationGroups = {PaNullCheck.class, PaFieldCheck.class, PaCrossFieldCheck.class};

    private Class[] paSubmissionGroups = {PaNullCheck.class, PaFieldCheck.class,
        PaCrossFieldCheck.class, PaSubmission.class};

    private Class[] intestacyValidationGroups = {IntestacyNullCheck.class, IntestacyFieldCheck.class,
        IntestacyCrossFieldCheck.class};

    private Class[] intestacySubmissionGroups = {IntestacyNullCheck.class, IntestacyFieldCheck.class,
        IntestacyCrossFieldCheck.class, IntestacySubmission.class};

    private Class[] caveatValidationGroups = {Default.class};

    private Class[] caveatSubmissionGroups = {Default.class};


    private Validator validator;

    private ValidationServiceImpl validationService;

    @BeforeEach
    public void setUp() {
        validator = Mockito.mock(Validator.class);
        validationService = new ValidationServiceImpl(validator);
    }

    @Test
    public void shouldValidateForGrantOfRepresentationGrantOfProbate() {
        GrantOfRepresentationData grantOfRepresentationData = GrantOfRepresentationData.builder()
            .grantType(GrantType.GRANT_OF_PROBATE)
            .build();
        CaseInfo caseInfo = CaseInfo.builder().build();

        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseData(grantOfRepresentationData)
            .caseInfo(caseInfo)
            .build();

        when(validator.validate(grantOfRepresentationData, paValidationGroups)).thenReturn(Sets.newHashSet());

        validationService.validate(probateCaseDetails);

        verify(validator, times(1)).validate(grantOfRepresentationData, paValidationGroups);
    }

    @Test
    public void shouldValidateForSubmissionGrantOfRepresentationGrantOfProbate() {
        GrantOfRepresentationData grantOfRepresentationData = GrantOfRepresentationData.builder()
            .grantType(GrantType.GRANT_OF_PROBATE)
            .build();
        CaseInfo caseInfo = CaseInfo.builder().build();

        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseData(grantOfRepresentationData)
            .caseInfo(caseInfo)
            .build();

        when(validator.validate(grantOfRepresentationData, paSubmissionGroups)).thenReturn(Sets.newHashSet());

        validationService.validateForSubmission(probateCaseDetails);

        verify(validator, times(1)).validate(grantOfRepresentationData, paSubmissionGroups);
    }

    @Test
    public void shouldThrowCaseValidationExceptionOnValidateWhenConstraintViolationsExist() {
        ConstraintViolation<GrantOfRepresentationData> constraintViolation = Mockito.mock(ConstraintViolation.class);
        Set<ConstraintViolation<GrantOfRepresentationData>> constraintViolations = new HashSet<>();
        constraintViolations.add(constraintViolation);

        GrantOfRepresentationData grantOfRepresentationData = GrantOfRepresentationData.builder()
            .grantType(GrantType.GRANT_OF_PROBATE)
            .build();
        CaseInfo caseInfo = CaseInfo.builder().build();

        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseData(grantOfRepresentationData)
            .caseInfo(caseInfo)
            .build();

        when(validator.validate(grantOfRepresentationData, paValidationGroups)).thenReturn(constraintViolations);

        assertThrows(CaseValidationException.class, () -> {
            validationService.validate(probateCaseDetails);
        });

        verify(validator, times(1)).validate(grantOfRepresentationData, paValidationGroups);
    }

    @Test
    public void shouldValidateForGrantOfRepresentationGrantOfIntestacy() {
        GrantOfRepresentationData grantOfRepresentationData = GrantOfRepresentationData.builder()
            .grantType(GrantType.INTESTACY)
            .build();
        CaseInfo caseInfo = CaseInfo.builder().build();

        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseData(grantOfRepresentationData)
            .caseInfo(caseInfo)
            .build();

        when(validator.validate(grantOfRepresentationData, intestacyValidationGroups)).thenReturn(Sets.newHashSet());

        validationService.validate(probateCaseDetails);

        verify(validator, times(1)).validate(grantOfRepresentationData, intestacyValidationGroups);
    }

    @Test
    public void shouldValidateForSubmissionGrantOfRepresentationIntestacy() {
        GrantOfRepresentationData grantOfRepresentationData = GrantOfRepresentationData.builder()
            .grantType(GrantType.INTESTACY)
            .build();
        CaseInfo caseInfo = CaseInfo.builder().build();

        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseData(grantOfRepresentationData)
            .caseInfo(caseInfo)
            .build();

        when(validator.validate(grantOfRepresentationData, intestacySubmissionGroups)).thenReturn(Sets.newHashSet());

        validationService.validateForSubmission(probateCaseDetails);

        verify(validator, times(1)).validate(grantOfRepresentationData, intestacySubmissionGroups);
    }


    @Test
    public void shouldValidateForCaveat() {
        CaveatData caveatData = CaveatData.builder().build();
        CaseInfo caseInfo = CaseInfo.builder().build();

        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseData(caveatData)
            .caseInfo(caseInfo)
            .build();

        when(validator.validate(caveatData, caveatValidationGroups)).thenReturn(Sets.newHashSet());

        validationService.validate(probateCaseDetails);

        verify(validator, times(1)).validate(caveatData, caveatValidationGroups);
    }

    @Test
    public void shouldValidateForSubmissionCaveat() {
        CaveatData caveatData = CaveatData.builder().build();
        CaseInfo caseInfo = CaseInfo.builder().build();

        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder()
            .caseData(caveatData)
            .caseInfo(caseInfo)
            .build();

        when(validator.validate(caveatData, caveatSubmissionGroups)).thenReturn(Sets.newHashSet());

        validationService.validateForSubmission(probateCaseDetails);

        verify(validator, times(1)).validate(caveatData, caveatSubmissionGroups);
    }
}
