package uk.gov.hmcts.probate.services.submit.validation.validator;

import uk.gov.hmcts.probate.services.submit.validation.ValidationRule;
import uk.gov.hmcts.reform.probate.model.Relationship;
import uk.gov.hmcts.reform.probate.model.cases.MaritalStatus;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import java.util.Arrays;
import java.util.List;

public class IntestacyValidator implements CaseDataValidator<GrantOfRepresentationData> {

    private static ValidationRule<GrantOfRepresentationData> isAliasNameListPopulated() {
        return ValidationRule.from(gop ->
                ValidatorUtils.allValuesNotNull(gop.getDeceasedAnyOtherNames())
                        && (gop.getDeceasedAnyOtherNames()
                        && (gop.getDeceasedAliasNameList() == null
                        || gop.getDeceasedAliasNameList().isEmpty())), "DeceasedAliasNameList is empty");
    }

    private static ValidationRule<GrantOfRepresentationData> isDeceasedAssetsOutsideUKPopulated() {
        return ValidationRule.from(gop -> ValidatorUtils.allValuesNotNull(gop.getIhtNetValue()) && (
                        gop.getIhtNetValue() <= 2500000L && gop.getDeceasedHasAssetsOutsideUK() == null)
                , "DeceasedHasAssetsOutsideUK is Null");
    }

    private static ValidationRule<GrantOfRepresentationData> isDeceasedDateOfDeathAfterDateOfBirth() {
        return ValidationRule.from(gop -> ValidatorUtils.allValuesNotNull(gop.getDeceasedDateOfDeath(), gop.getDeceasedDateOfDeath()) &&
                        gop.getDeceasedDateOfDeath().isBefore(gop.getDeceasedDateOfBirth())
                , "DeceasedDateOfDeath before DeceasedDateOfBirth");
    }

    private static ValidationRule<GrantOfRepresentationData>
    isDeceasedOtherChildPopulatedWhenRelationshipToDeceasedIsAdoptedChild() {
        return ValidationRule.from(gop -> ValidatorUtils.allValuesNotNull(gop.getPrimaryApplicantRelationshipToDeceased(), gop.getPrimaryApplicantAdoptionInEnglandOrWales()) &&
                        (gop.getPrimaryApplicantRelationshipToDeceased().equals(Relationship.ADOPTED_CHILD)
                                && !gop.getPrimaryApplicantAdoptionInEnglandOrWales()
                                && gop.getDeceasedOtherChildren() == null)
                , "RelationshipToDeceasedIsAdoptedChild and DeceasedOtherChildren is Null");
    }

    private static ValidationRule<GrantOfRepresentationData>
    isDeceasedOtherChildPopulatedWhenRelationshipToDeceasedIsChild() {
        return ValidationRule.from(gop -> ValidatorUtils.allValuesNotNull(gop.getPrimaryApplicantRelationshipToDeceased())
                        && (gop.getPrimaryApplicantRelationshipToDeceased().equals(Relationship.CHILD)
                        && gop.getDeceasedOtherChildren() == null)
                , "RelationshipToDeceasedIsChild and DeceasedOtherChildren is Null");
    }

    private static ValidationRule<GrantOfRepresentationData>
    isDivorcedInEnglandOrWalesPopulatedWhenDeceasedDivorced() {
        return ValidationRule.from(gop -> ValidatorUtils.allValuesNotNull(gop.getDeceasedMartialStatus())
                        && (gop.getDeceasedMartialStatus().equals(MaritalStatus.DIVORCED)
                        && gop.getDeceasedDivorcedInEnglandOrWales() == null)
                , "DeceasedMaritalStatusIsDivorced and DivorcedInEnglandOrWales is Null");
    }

    private static ValidationRule<GrantOfRepresentationData>
    isDivorcedInEnglandOrWalesPopulatedWhenDeceasedSeperated() {
        return ValidationRule.from(gop -> ValidatorUtils.allValuesNotNull(gop.getDeceasedMartialStatus())
                        && (gop.getDeceasedMartialStatus().equals(MaritalStatus.JUDICIALLY_SEPARATED)
                        && gop.getDeceasedDivorcedInEnglandOrWales() == null)
                , "DeceasedMaritalStatusIsSeparated and DivorcedInEnglandOrWales is Null");
    }

    private static ValidationRule<GrantOfRepresentationData>
    isAllDeceasedChildrenOverEighteenPopulatedWhenDeceasedHasOtherChildren() {
        return ValidationRule.from(gop ->
                        ValidatorUtils.allValuesNotNull(gop.getDeceasedOtherChildren()) && (gop.getDeceasedOtherChildren()
                                && gop.getChildrenOverEighteenSurvived() == null)
                , "DeceasedHasOtherChildren and AllDeceasedChildrenOverEighteen is Null");
    }

    private static ValidationRule<GrantOfRepresentationData> isChildrenDiedPopulatedWhenDeceasedHasOtherChildren() {
        return ValidationRule.from(gop ->
                        (ValidatorUtils.allValuesNotNull(gop.getDeceasedOtherChildren()) &&
                                (gop.getDeceasedOtherChildren() && gop.getChildrenDied() == null))
                , "ChildrenDied is Null");
    }

    private static ValidationRule<GrantOfRepresentationData>
    isGrandChildrenSurvivedUnderEighteenPopulatedWhenMandatory() {
        return ValidationRule.from(gop ->
                        (ValidatorUtils.allValuesNotNull(gop.getDeceasedOtherChildren(), gop.getChildrenOverEighteenSurvived(), gop.getChildrenDied()))
                                && (gop.getDeceasedOtherChildren() && gop.getChildrenDied()
                                && gop.getGrandChildrenSurvivedUnderEighteen() == null)
                , "GrandChildrenSurvivedUnderEighteen is Null");
    }

    @Override
    public List<ValidationRule<GrantOfRepresentationData>> getRules() {
        return Arrays.asList(
                IntestacyValidator.isAliasNameListPopulated(),
                IntestacyValidator.isDeceasedAssetsOutsideUKPopulated(),
                IntestacyValidator.isDeceasedDateOfDeathAfterDateOfBirth(),
                IntestacyValidator.isAllDeceasedChildrenOverEighteenPopulatedWhenDeceasedHasOtherChildren(),
                IntestacyValidator.isChildrenDiedPopulatedWhenDeceasedHasOtherChildren(),
                IntestacyValidator.isDeceasedOtherChildPopulatedWhenRelationshipToDeceasedIsAdoptedChild(),
                IntestacyValidator.isDivorcedInEnglandOrWalesPopulatedWhenDeceasedDivorced(),
                IntestacyValidator.isDeceasedOtherChildPopulatedWhenRelationshipToDeceasedIsChild(),
                IntestacyValidator.isDivorcedInEnglandOrWalesPopulatedWhenDeceasedSeperated(),
                IntestacyValidator.isGrandChildrenSurvivedUnderEighteenPopulatedWhenMandatory()
        );
    }
}
